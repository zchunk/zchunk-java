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
import io.github.zchunk.fileformat.util.ByteUtils;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/*
 * Current flags are:
 *  bit 0: File has data streams
 *  bit 1: File has optional elements
 */
public enum PrefaceFlag {
  HAS_DATA_STREAMS(0b00000001L),
  HAS_OPTIONAL_ELEMENTS(0b00000010L);

  private final long bitflag;

  PrefaceFlag(final long flag) {
    this.bitflag = flag;
  }

  public static Set<PrefaceFlag> getPrefaceFlags(final CompressedInt ci) {
    final AtomicReference<BigInteger> remainingFlagLong = new AtomicReference<>(ci.getValue());

    return getPrefaceFlags(remainingFlagLong);
  }

  private static Set<PrefaceFlag> getPrefaceFlags(final AtomicReference<BigInteger> remainingFlagLong) {
    final Set<PrefaceFlag> foundFlags = Arrays.stream(PrefaceFlag.values())
        .filter(currentFlag -> ByteUtils.decrease(remainingFlagLong, currentFlag.getBitflag()))
        .collect(Collectors.toSet());

    if (remainingFlagLong.get().longValue() != 0L) {
      throw new UnsupportedOperationException(
          "Flags not supported yet: [" + ByteUtils.longToBinaryString(remainingFlagLong.get().longValue()) + "].");
    }

    return foundFlags;
  }

  public long getBitflag() {
    return this.bitflag;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", PrefaceFlag.class.getSimpleName() + "[", "]")
        .add("bitflag=" + this.bitflag)
        .toString();
  }
}
