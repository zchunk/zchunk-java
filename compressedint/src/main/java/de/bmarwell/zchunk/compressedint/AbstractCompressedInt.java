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

package de.bmarwell.zchunk.compressedint;

import java.math.BigInteger;
import java.util.StringJoiner;
import org.immutables.value.Value;

@Value.Immutable
public abstract class AbstractCompressedInt implements CompressedInt {

  @Override
  @Value.Lazy
  public BigInteger getValue() {
    return CompressedIntUtil.decompress(getCompressedBytes());
  }

  @Override
  @Value.Lazy
  public long getLongValue() {
    return getValue().longValueExact();
  }

  @Override
  @Value.Lazy
  public long getUnsignedLongValue() {
    return getValue().longValue();
  }

  @Override
  @Value.Lazy
  public int getIntValue() {
    return getValue().intValueExact();
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", CompressedInt.class.getSimpleName() + "[", "]")
        .add("compressedBytes=" + new BigInteger(1, getCompressedBytes()).toString(16))
        .add("unsignedValue=" + getValue().toString())
        .toString();
  }
}
