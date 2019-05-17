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

import static java.util.stream.Collectors.toMap;

import de.bmarwell.zchunk.compression.algo.unknown.UnknownAlgorithm;
import de.bmarwell.zchunk.compression.api.internal.ReflectionUtil;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class CompressionAlgorithmFactory {

  private static final Package THIS_PACKAGE = CompressionAlgorithmFactory.class.getPackage();
  private static final String ROOT_PACKAGE = THIS_PACKAGE.getName().replaceAll("\\.api$", ".algo");

  private CompressionAlgorithmFactory() {
    // util class.
  }

  private static Map.@Nullable Entry<Long, Class<CompressionAlgorithm>> mapEntryOrNull(final Class<CompressionAlgorithm> clazz) {
    return ReflectionUtil.newInstance(clazz)
        .map(compInstance -> new AbstractMap.SimpleEntry<>(compInstance.getCompressionTypeValue().getLongValue(), clazz))
        .orElse(null);
  }

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

    private static ResourceHolder INSTANCE = null;

    private final List<Class<CompressionAlgorithm>> implementations;

    private final Map<Long, Class<CompressionAlgorithm>> typeMapping;

    public ResourceHolder(final String rootPackage) {
      this.implementations = ReflectionUtil.loadImplementations(rootPackage, CompressionAlgorithm.class);
      this.typeMapping = loadTypeMapping(this.implementations);
    }

    public static ResourceHolder newInstance(final String rootPackage) {
      if (INSTANCE == null) {
        INSTANCE = new ResourceHolder(rootPackage);
      }

      return INSTANCE;
    }

    private Map<Long, Class<CompressionAlgorithm>> loadTypeMapping(final List<Class<CompressionAlgorithm>> implementations) {
      return implementations.stream()
          .map(CompressionAlgorithmFactory::mapEntryOrNull)
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
