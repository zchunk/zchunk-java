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

package de.bmarwell.zchunk.compression.api;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import de.bmarwell.zchunk.compression.algo.unknown.UnknownAlgorithm;
import de.bmarwell.zchunk.compression.api.internal.ReflectionUtil;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class CompressionAlgorithmFactory {

  // do not change this as this would break plugins.
  private static final String ROOT_PACKAGE = "io.github.zchunk.compression";

  private CompressionAlgorithmFactory() {
    // util class.
  }

  private static Map.@Nullable Entry<Long, Class<CompressionAlgorithm>> mapEntryOrNull(final Class<CompressionAlgorithm> clazz) {
    return ReflectionUtil.newInstance(clazz)
        .map(compInstance -> new AbstractMap.SimpleEntry<>(compInstance.getCompressionTypeValue().getLongValue(), clazz))
        .orElse(null);
  }

  /**
   * Tries to load a compression algorithm. If it fails, it will return {@link UnknownAlgorithm}.
   *
   * <p><b>Warning!</b> Unstable API, this might change to Optional some day.</p>
   *
   * @param compressionType
   *     the compression type value as defined in {@code zchunk_format.txt}.
   * @return a compression algorithm which can decompress an input stream.
   */
  public static CompressionAlgorithm forType(final long compressionType) {
    return Optional.ofNullable(getTypeMappings().get(compressionType))
        .flatMap(ReflectionUtil::newInstance)
        .orElseGet(UnknownAlgorithm::new);
  }

  /* Utility methods */

  private static List<Class<CompressionAlgorithm>> getImplementations() {
    return ResourceHolder.newInstance(ROOT_PACKAGE).getImplementations();
  }

  private static Map<Long, Class<CompressionAlgorithm>> getTypeMappings() {
    return ResourceHolder.newInstance(ROOT_PACKAGE).getTypeMapping();
  }


  private static class ResourceHolder {

    private static @Nullable ResourceHolder INSTANCE = null;

    private final List<Class<CompressionAlgorithm>> implementations;

    private final Map<Long, Class<CompressionAlgorithm>> typeMapping;

    public ResourceHolder(final String rootPackage) {
      this.implementations = loadImplementations(rootPackage, CompressionAlgorithm.class);
      this.typeMapping = loadTypeMapping(this.implementations);
    }

    @EnsuresNonNull({"INSTANCE"})
    public static ResourceHolder newInstance(final String rootPackage) {
      if (INSTANCE == null) {
        INSTANCE = new ResourceHolder(rootPackage);
      }

      return INSTANCE;
    }

    /**
     * Will try to load classes implementing clazz, from any package below rootpackage.
     *
     * @param rootPackage
     *     the root package to search in.
     * @param clazz
     *     the class which should be implemented by the found classes.
     * @param <T>
     *     the class type.
     * @return a list of classes implementing T / clazz.
     */
    public <T> List<Class<T>> loadImplementations(@UnderInitialization ResourceHolder this,
        final String rootPackage,
        final Class<T> clazz) {
      final List<Class<T>> classes = ReflectionUtil.getClasses(rootPackage, clazz);

      return classes.stream()
          .filter(ReflectionUtil.classImplementsCompressionAlgorithm(clazz))
          .collect(toList());
    }

    private Map<Long, Class<CompressionAlgorithm>> loadTypeMapping(@UnderInitialization ResourceHolder this,
        final List<Class<CompressionAlgorithm>> implementations) {
      final Stream<@Nullable Entry<Long, Class<CompressionAlgorithm>>> entryStream = implementations.stream()
          .map(CompressionAlgorithmFactory::mapEntryOrNull);
      return entryStream
          .filter(Objects::nonNull)
          .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public List<Class<CompressionAlgorithm>> getImplementations() {
      return this.implementations;
    }

    public Map<Long, Class<CompressionAlgorithm>> getTypeMapping() {
      return this.typeMapping;
    }
  }
}
