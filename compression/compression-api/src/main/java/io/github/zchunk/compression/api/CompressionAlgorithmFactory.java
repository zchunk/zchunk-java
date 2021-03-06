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

package io.github.zchunk.compression.api;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;

import io.github.zchunk.compressedint.CompressedInt;
import io.github.zchunk.compression.algo.unknown.UnknownAlgorithm;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.checkerframework.checker.initialization.qual.UnderInitialization;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class CompressionAlgorithmFactory {

  // do not change this as this would break plugins.
  private static final String ROOT_PACKAGE = "io.github.zchunk.compression";

  private CompressionAlgorithmFactory() {
    // util class.
  }

  public static Map<Long, CompressionAlgorithm> getKnownAlgorithms() {
    return unmodifiableMap(getTypeMappings());
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
        .orElseGet(UnknownAlgorithm::new);
  }

  /* Utility methods */

  private static List<CompressionAlgorithm> getImplementations() {
    return ResourceHolder.newInstance(ROOT_PACKAGE).getImplementations();
  }

  private static Map<Long, CompressionAlgorithm> getTypeMappings() {
    return ResourceHolder.newInstance(ROOT_PACKAGE).getTypeMapping();
  }

  public static CompressionAlgorithm forType(final CompressedInt compressedInt) {
    return forType(compressedInt.getLongValue());
  }


  private static class ResourceHolder {

    private static @Nullable ResourceHolder INSTANCE = null;

    private final List<CompressionAlgorithm> implementations;

    private final Map<Long, CompressionAlgorithm> typeMapping;

    public ResourceHolder() {
      this.implementations = loadImplementations();
      this.typeMapping = loadTypeMapping(this.implementations);
    }

    @EnsuresNonNull({"INSTANCE"})
    public static ResourceHolder newInstance(final String rootPackage) {
      if (INSTANCE == null) {
        INSTANCE = new ResourceHolder();
      }

      return INSTANCE;
    }

    /**
     * Will try to load classes implementing clazz, from any package below rootpackage.
     *
     * @return a list of classes implementing T / clazz.
     */
    public List<CompressionAlgorithm> loadImplementations(@UnderInitialization ResourceHolder this) {
      final ServiceLoader<CompressionAlgorithm> load = ServiceLoader.load(CompressionAlgorithm.class);
      final Logger logger = Logger.getLogger("io.github.zchunk");

      logger.finest("ServiceLoader: " + load);

      return StreamSupport.stream(load.spliterator(), false)
          .peek(clazz2 -> logger.finer("Class: " + clazz2.getClass()))
          .collect(Collectors.toList());
    }

    private Map<Long, CompressionAlgorithm> loadTypeMapping(@UnderInitialization ResourceHolder this,
                                                            final List<CompressionAlgorithm> implementations) {
      final Stream<@Nullable Entry<Long, CompressionAlgorithm>> entryStream = implementations.stream()
          .map(ResourceHolder::mapEntryOrNull);
      return entryStream
          .filter(Objects::nonNull)
          .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public List<CompressionAlgorithm> getImplementations() {
      return this.implementations;
    }

    public Map<Long, CompressionAlgorithm> getTypeMapping() {
      return this.typeMapping;
    }

    private static Map.Entry<Long, CompressionAlgorithm> mapEntryOrNull(final CompressionAlgorithm clazz) {
      return new AbstractMap.SimpleEntry<>(clazz.getCompressionTypeValue().getUnsignedLongValue(), clazz);
    }
  }

}
