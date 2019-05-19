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

package io.github.zchunk.fileformat;

import io.github.zchunk.compressedint.CompressedInt;
import java.math.BigInteger;
import org.immutables.value.Value;

@Value.Immutable
public interface OptionalElement {

  CompressedInt getId();

  CompressedInt getDataSize();

  byte[] getData();

  @Value.Derived
  default long getTotalLength() {
    return BigInteger.valueOf(getId().getCompressedBytes().length)
        .add(BigInteger.valueOf(getDataSize().getCompressedBytes().length))
        // either this or getData().length
        .add(getDataSize().getValue())
        .longValueExact();
  }
}
