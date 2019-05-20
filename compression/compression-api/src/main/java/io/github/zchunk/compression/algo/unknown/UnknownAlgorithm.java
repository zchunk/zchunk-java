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

package io.github.zchunk.compression.algo.unknown;

import io.github.zchunk.compressedint.CompressedInt;
import io.github.zchunk.compressedint.CompressedIntFactory;
import io.github.zchunk.compression.api.CompressionAlgorithm;
import io.github.zchunk.compression.api.err.DecompressionException;
import java.io.InputStream;
import java.util.function.BiFunction;

public class UnknownAlgorithm implements CompressionAlgorithm {

  @Override
  public CompressedInt getCompressionTypeValue() {
    return CompressedIntFactory.valueOf(-1L);
  }

  @Override
  public String getName() {
    return "unknown";
  }

  @Override
  public BiFunction<InputStream, byte[], InputStream> getOutputStreamSupplier() {
    throw new DecompressionException("Could not a valid decompressor implementation.");
  }
}
