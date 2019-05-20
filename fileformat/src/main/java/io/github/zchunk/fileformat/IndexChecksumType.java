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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.logging.Logger;

/**
 * Checksum type for everything in the index.
 *
 * <p>
 * Current values:
 * 0 = SHA-1<br>
 * 1 = SHA-256<br>
 * 2 = SHA-512<br>
 * 3 = SHA-512/128 (first 128 bits of SHA-512 checksum)<br>
 * </p>
 */
public enum IndexChecksumType {
  UNKNOWN("none", -1L, 0),
  SHA1("SHA-1", 0L, -1),
  SHA256("SHA-256", 1L, -1),
  SHA512("SHA-512", 2L, -1),
  SHA512_128("SHA-512", 3L, 16);

  private final String digestAlgorithm;
  private final int length;
  private final long identifier;

  IndexChecksumType(final String digestAlgorithm, final long identifier, final int length) {
    try {
      this.digestAlgorithm = digestAlgorithm;
      this.identifier = identifier;
      if (length != -1) {
        this.length = length;
      } else {
        this.length = MessageDigest.getInstance(digestAlgorithm).getDigestLength();
      }
    } catch (final NoSuchAlgorithmException algoEx) {
      throw new IllegalArgumentException("Unable to create hashing algorithm: [" + digestAlgorithm + "]. Check your JVM settings.", algoEx);
    }
  }

  public static IndexChecksumType find(final BigInteger unsignedLongValue) {
    if (unsignedLongValue.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) >= 1) {
      final String message = String.format("Unknown Checksum type: [%s], exceeds [%d]!", unsignedLongValue.toString(), Integer.MAX_VALUE);
      Logger.getLogger(IndexChecksumType.class.getCanonicalName()).warning(message);
      return UNKNOWN;
    }

    final long requestedId = unsignedLongValue.longValue();

    return Arrays.stream(values())
        .filter(algo -> algo.identifier == requestedId)
        .findFirst()
        .orElse(UNKNOWN);
  }

  public int actualChecksumLength() {
    return this.length;
  }

  public byte[] digest(final byte[] input) {
    final byte[] digest = this.getMessageDigest().digest(input);

    if (this.length != getMessageDigest().getDigestLength()) {
      final byte[] actualDigest = new byte[this.length];
      System.arraycopy(digest, 0, actualDigest, 0, this.length);
      return actualDigest;
    }

    return digest;
  }

  public MessageDigest getMessageDigest() {
    try {
      return MessageDigest.getInstance(this.digestAlgorithm);
    } catch (final NoSuchAlgorithmException algoEx) {
      throw new IllegalStateException("Unable to create message getMessageDigest instance!", algoEx);
    }
  }

  public long getIdentifier() {
    return this.identifier;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", IndexChecksumType.class.getSimpleName() + "[", "]")
        .add("digestAlgorithm=" + this.digestAlgorithm)
        .add("actualChecksumLength=" + this.length)
        .add("identifier=" + this.identifier)
        .add("ordinal=" + this.ordinal())
        .toString();
  }
}
