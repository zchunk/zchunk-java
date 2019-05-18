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

import de.bmarwell.zchunk.compressedint.CompressedInt;
import java.io.InputStream;
import java.util.function.Function;
import org.immutables.value.Value;

@Value.Immutable
public interface CompressionAlgorithm {

  /**
   * The type value as defined by {@code zchunk_format.txt}.
   *
   * @return the compressiontype as CompressedInt.
   */
  CompressedInt getCompressionTypeValue();

  /**
   * A user-friendly name of the compression algorithm.
   *
   * @return the name of the compression algorithm.
   */
  String getName();

  /**
   * A method that will take in a stream and output an uncompressed stream.
   * @return a stream conversion method.
   */
  Function<InputStream, InputStream> getOutputStreamSupplier();
}
