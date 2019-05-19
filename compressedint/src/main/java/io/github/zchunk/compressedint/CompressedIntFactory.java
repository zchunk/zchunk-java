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

import java.io.IOException;
import java.io.InputStream;

public final class CompressedIntFactory {

  private CompressedIntFactory() {
    // util class
  }

  /**
   * Try to parse a compressed in from a given byte array.
   *
   * @param input
   *     the input byte array.
   * @return a compressed int if it was parseable.
   * @throws IllegalArgumentException
   *     if the byte array has zero-length.
   * @throws IllegalArgumentException
   *     if the byte array length (byte count) is larger than {@link CompressedIntUtil#MAX_COMPRESSED_INT_LENGTH}.
   * @throws NullPointerException
   *     if input is {@code null}.
   */
  public static CompressedInt fromCompressedBytes(final byte[] input) {
    if (input.length > CompressedIntUtil.MAX_COMPRESSED_INT_LENGTH) {
      throw new IllegalArgumentException(
          "Input length [" + input.length + "] bytes is too large! Max allowed: " + CompressedIntUtil.MAX_COMPRESSED_INT_LENGTH);
    }

    return ImmutableCompressedInt.builder()
        .compressedBytes(input)
        .build();
  }

  /**
   * Convert an unsigned long to a compressed int.
   *
   * <p>Hint: If your long yields -1L, it's instead {@code 0xffffffffffffffff}. But java treats it as
   * signed long.</p>
   *
   * @param unsignedLongValue
   *     a long value which gets interpreted as unsigned.
   * @return a compressedInt.
   */
  public static CompressedInt valueOf(final long unsignedLongValue) {
    final byte[] unsignedBytes = CompressedIntUtil.compress(unsignedLongValue);

    return fromCompressedBytes(unsignedBytes);
  }

  /**
   * Directly takes an open {@link InputStream} and tries to read as many bytes as it takes for the compressed integer to end.
   *
   * <p>Does not close the stream. That is the caller‘s responsibility.</p>
   *
   * @param inputStream
   *     the not-closed stream to read from.
   * @return a compresssed int.
   * @throws IOException
   *     if we cannot read from the underlying stream.
   * @throws IllegalArgumentException
   *     if the compressed int size (bytes read) will exceed {@link CompressedIntUtil#MAX_COMPRESSED_INT_LENGTH}.
   */
  public static CompressedInt readCompressedInt(final InputStream inputStream) throws IOException {
    int currentByte;
    int byteCounter = 0;
    final byte[] buffer = new byte[CompressedIntUtil.MAX_COMPRESSED_INT_LENGTH];

    while ((currentByte = inputStream.read()) != -1) {
      if (byteCounter >= CompressedIntUtil.MAX_COMPRESSED_INT_LENGTH) {
        throw new IllegalArgumentException("CompressedInt too large. Giving up after reading [" + byteCounter + "] bytes.");
      }

      buffer[byteCounter] = (byte) currentByte;

      if ((currentByte & CompressedIntUtil.COMPRESSED_INT_LAST_BYTE_FLAG) == CompressedIntUtil.COMPRESSED_INT_LAST_BYTE_FLAG) {
        break;
      }

      byteCounter++;
    }

    final byte[] read = new byte[byteCounter + 1];
    System.arraycopy(buffer, 0, read, 0, byteCounter + 1);

    return fromCompressedBytes(read);
  }
}
