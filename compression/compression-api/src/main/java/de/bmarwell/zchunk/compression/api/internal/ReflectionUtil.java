/*
 * Copyright 2019, the zchunk-java contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.bmarwell.zchunk.compression.api.internal;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ReflectionUtil {

  private static final Logger LOG = Logger.getLogger(ReflectionUtil.class.getCanonicalName());

  private ReflectionUtil() {
    // util
  }

  public static <T> List<Class<T>> loadImplementations(final String rootPackage, final Class<T> clazz) {
    final List<Class<T>> classes = getClasses(rootPackage, clazz);

    return classes.stream()
        .filter(classImplementsCompressionAlgorithm(clazz))
        .collect(toList());
  }


  private static <T> List<Class<T>> getClasses(final String rootPackage, final Class<T> targetClazz) {
    try {
      final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      final String path = rootPackage.replace('.', '/');
      final Enumeration<URL> resources = classLoader.getResources(path);
      final List<File> dirs = new ArrayList<>();

      while (resources.hasMoreElements()) {
        final URL resource = resources.nextElement();
        dirs.add(new File(resource.getFile()));
      }

      return dirs.stream()
          .map(dir -> findClasses(dir, rootPackage, targetClazz))
          .flatMap(Collection::stream)
          .collect(toList());
    } catch (final IOException ioEx) {
      return emptyList();
    }
  }


  /**
   * Recursive method used to find all classes in a given directory and subdirs.
   *
   * @param directory
   *     The base directory
   * @param packageName
   *     The package name for classes found inside the base directory
   * @return The classes
   */
  private static <T> List<Class<T>> findClasses(final File directory, final String packageName, final Class<T> clazzType) {
    if (!directory.exists()) {
      return Collections.emptyList();
    }

    final List<File> files = getListFromArray(directory.listFiles());

    return files.stream()
        .map(file -> findClasses(packageName, clazzType, file))
        .flatMap(Collection::stream)
        .collect(toList());
  }

  private static <T> List<Class<T>> findClasses(final String packageName, final Class<T> clazzType, final File file) {
    if (file.isDirectory()) {
      if (file.getName().contains(".")) {
        // bad match.
        return emptyList();
      }

      return findClasses(file, packageName + "." + file.getName(), clazzType);
    }

    if (file.getName().endsWith(".class")) {
      final @Nullable Class<T> aClass = loadClass(packageName, clazzType, file);
      if (aClass != null) {
        return singletonList(aClass);
      }
    }

    return emptyList();
  }

  @Nullable
  private static <T> Class<T> loadClass(final String packageName, final Class<T> clazzType, final File file) {
    try {
      final Class<?> aClass = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
      if (classImplementsCompressionAlgorithm(clazzType).test(aClass)) {
        return (Class<T>) aClass;
      }
    } catch (final ClassNotFoundException e) {
      LOG.log(Level.WARNING, e, () -> String.format("Class file [%s] found, but unable to create instance.", file.getAbsolutePath()));
    }

    return null;
  }

  private static <T> List<T> getListFromArray(final T[] input) {
    return Optional.ofNullable(input)
        .map(Arrays::asList)
        .orElseGet(Collections::emptyList);
  }

  public static <T> Optional<T> newInstance(final Class<T> clazz) {
    try {
      return Optional.of(clazz.newInstance());
    } catch (final InstantiationException | IllegalAccessException e) {
      LOG.log(Level.WARNING, e, () -> String.format("Unable to instantiate class [%s]. Skipping.", clazz));
      return Optional.empty();
    }
  }

  private static <T> Predicate<Class<?>> classImplementsCompressionAlgorithm(final Class<T> type) {
    return clazz -> getListFromArray(type.getInterfaces()).contains(type);
  }

}
