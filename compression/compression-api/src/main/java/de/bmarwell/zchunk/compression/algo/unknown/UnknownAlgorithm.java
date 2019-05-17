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

package de.bmarwell.zchunk.compression.algo.unknown;

import de.bmarwell.zchunk.compressedint.CompressedInt;
import de.bmarwell.zchunk.compressedint.CompressedIntFactory;
import de.bmarwell.zchunk.compression.api.CompressionAlgorithm;
import java.io.InputStream;
import java.util.function.Function;

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
  public Function<InputStream, InputStream> getOutputStreamSupplier() {
    throw new UnsupportedOperationException("not implemented");
  }
}
