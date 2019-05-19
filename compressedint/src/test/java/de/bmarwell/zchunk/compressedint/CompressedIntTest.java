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

package io.github.zchunk.compressedint;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CompressedIntTest {

  private static final Logger LOG = Logger.getLogger(CompressedIntTest.class.getCanonicalName());

  private static final byte[] CP_394 = new byte[]{(byte) 0b00001010, (byte) 0b10000011};

  public static String byteArrayToBinaryString(final byte[] input) {
    return new BigInteger(1, input).toString(2);
  }

  @Test
  public void testToCompressedInt() {
    final CompressedInt ci = CompressedIntFactory.valueOf(394L);

    final String binaryString = byteArrayToBinaryString(ci.getCompressedBytes());

    Assertions.assertEquals("101010000011", binaryString);
  }

  @Test
  public void testToCompressedInt_max() {
    final CompressedInt bytes = CompressedIntFactory.valueOf(Integer.MAX_VALUE);

    final String binaryString = byteArrayToBinaryString(bytes.getCompressedBytes());

    Assertions.assertEquals("111111101111111011111110111111110000111", binaryString);
  }

  @Test
  public void testToCompressedInt_ulong_max() {
    final byte[] ulongAsBytes = new byte[8];
    Arrays.fill(ulongAsBytes, (byte) 0xff);
    final long unsignedLongValue = new BigInteger(1, ulongAsBytes).longValue();

    final CompressedInt bytes = CompressedIntFactory.valueOf(unsignedLongValue);

    final String binaryString = byteArrayToBinaryString(bytes.getCompressedBytes());

    Assertions.assertEquals("1111111011111110111111101111111011111110111111101111111011111110111111110000001", binaryString);
  }

  @Test
  public void testReadCompressedInt() {
    final byte[] maxLong = new byte[8];
    Arrays.fill(maxLong, (byte) 0xff);
    final CompressedInt compressedInt = CompressedIntFactory.fromCompressedBytes(maxLong);

    Assertions.assertAll(
        () -> Assertions.assertEquals("18446744073709551615", compressedInt.getValue().toString())
    );
  }

  @Test
  public void testReadCompressedInt_maxValue() {
    // 1111111 01111111 01111111 01111111 01111111 01111111 01111111 011111110 11111111 0000001
    // repeated max value on purpose: guard against changes.
    final byte[] bytes = new byte[]{
        0b1111111, 0b01111111, 0b01111111, 0b01111111, 0b01111111, 0b01111111, 0b01111111, (byte) 0b011111110, (byte) 0b11111111, 0b0000001
    };

    final CompressedInt compressedInt = CompressedIntFactory.fromCompressedBytes(bytes);

    Assertions.assertAll(
        () -> Assertions.assertEquals("18446744073709551615", compressedInt.getValue().toString())
    );
  }

  @Test
  public void testCompressedIntMaxLength() {
    Assertions.assertAll(
        () -> Assertions.assertEquals(10, CompressedIntUtil.MAX_COMPRESSED_INT_LENGTH),
        () -> Assertions.assertThrows(IllegalArgumentException.class, () -> CompressedIntFactory.fromCompressedBytes(new byte[11]))
    );
  }

  @Test
  public void testToCompressedInt_unsigned_394() {
    final CompressedInt ci = CompressedIntFactory.valueOf(394);

    Assertions.assertAll(
        () -> Assertions.assertEquals(2, ci.getCompressedBytes().length),
        () -> Assertions.assertArrayEquals(CP_394, ci.getCompressedBytes())
    );
  }

  @Test
  public void testFromBytes_6582() {
    final CompressedInt compressedInt = CompressedIntFactory.fromCompressedBytes(new byte[]{0x65, (byte) 0x82});

    Assertions.assertAll(
        () -> Assertions.assertEquals(2L, compressedInt.getCompressedBytes().length),
        () -> Assertions.assertEquals(357L, compressedInt.getLongValue())
    );
  }

  @Test
  public void testToUnsignedInt_zero() {
    final byte[] input = new byte[]{(byte) 0b10000000};
    final CompressedInt anInt = CompressedIntFactory.fromCompressedBytes(input);
    final long unsignedLong = anInt.getIntValue();

    Assertions.assertEquals(0L, unsignedLong);
  }

  @Test
  public void testToUnsignedInt_one() {
    final byte[] input = new byte[]{(byte) 0b10000001};
    final CompressedInt anInt = CompressedIntFactory.fromCompressedBytes(input);
    final long unsignedLong = anInt.getIntValue();

    Assertions.assertEquals(1L, unsignedLong);
  }

  @Test
  public void testToUnsignedInt() {
    final CompressedInt anInt = CompressedIntFactory.fromCompressedBytes(CP_394);
    final long unsignedLong = anInt.getLongValue();

    Assertions.assertEquals(394L, unsignedLong);
  }

  @Test
  public void testExceptionOnBigLongToInt() {
    final CompressedInt compressedInt = CompressedIntFactory.valueOf(0xffffffff00ffffL);

    Assertions.assertThrows(ArithmeticException.class, compressedInt::getIntValue);
  }

  @Test
  public void testExceptionOnBigLongToLong() {
    final CompressedInt compressedInt = CompressedIntFactory.valueOf(0xffffffff00ffffL);

    Assertions.assertThrows(ArithmeticException.class, compressedInt::getLongValue);
  }

  @Test
  public void testExceptionOnBigLongToUnsignedLong() {
    final CompressedInt compressedInt = CompressedIntFactory.valueOf(-1L);

    Assertions.assertEquals(-1L, compressedInt.getUnsignedLongValue());
  }

  @Test
  public void testExceptionOnBigLongToUnsignedLong_minus2() {
    final CompressedInt compressedInt = CompressedIntFactory.valueOf(-2L);

    Assertions.assertEquals(-2L, compressedInt.getUnsignedLongValue());
  }
}
