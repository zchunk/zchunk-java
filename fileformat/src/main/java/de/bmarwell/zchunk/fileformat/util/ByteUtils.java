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

package de.bmarwell.zchunk.fileformat.util;

import de.bmarwell.zchunk.compressedint.CompressedIntUtil;
import java.math.BigInteger;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class ByteUtils {

  private ByteUtils() {
    // util class
  }

  public static String byteArrayToHexString(final byte[] input) {
    if (input.length == 0) {
      return "";
    }

    final String hexString = new BigInteger(1, input).toString(16);

    if (hexString.length() < input.length * 2) {
      final int missing = input.length * 2 - hexString.length();
      final String zeros = new String(new char[missing]).replace("\0", "0");

      return zeros + hexString;
    }

    return hexString;
  }

  public static String byteArrayToBinaryString(final byte[] input) {
    return new BigInteger(1, input).toString(2);
  }

  public static String longToBinaryString(final long input) {
    return BigInteger.valueOf(input).toString(2);
  }

  public static byte[] hexStringToByteArray(final String inputString) {
    final String cleanString = inputString.replaceAll(" ", "").toLowerCase(Locale.ENGLISH);
    final int len = cleanString.length();

    if ((len % 2) != 0) {
      throw new IllegalArgumentException("not a valid hex string: [" + cleanString + "].");
    }

    if (!cleanString.matches("^([0-9a-f][0-9a-f])+$")) {
      throw new IllegalArgumentException("hex string does not consist of only hex chars: [" + cleanString + "].");
    }

    final byte[] data = new byte[len / 2];

    for (int i = 0; i < len; i += 2) {
      final int i1 = Character.digit(cleanString.charAt(i), 16) << 4;
      final int i2 = Character.digit(cleanString.charAt(i + 1), 16);

      data[i / 2] = (byte) (i1 + i2);
    }

    return data;
  }

  public static boolean decrease(final AtomicReference<BigInteger> remainingFlagLong, final long bitflag) {
    final AtomicBoolean changed = new AtomicBoolean();
    remainingFlagLong.getAndUpdate(curr -> getLongUnaryOperator(curr, bitflag, changed));

    return changed.get();
  }

  public static BigInteger getLongUnaryOperator(final BigInteger curr, final long bitflag, final AtomicBoolean changed) {
    // todo: check bigint is not bigger than (long.max * 2) + 1;
    final long longValue = curr.longValue();

    if ((longValue & bitflag) == bitflag) {
      changed.set(true);
      return BigInteger.valueOf(longValue & ~bitflag).and(CompressedIntUtil.UNSIGNED_LONG_MASK);
    }

    return BigInteger.valueOf(longValue).and(CompressedIntUtil.UNSIGNED_LONG_MASK);
  }


}
