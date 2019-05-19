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
 * 0 = SHA-1
 * 1 = SHA-256
 */
public enum HeaderChecksumType {
  UNKNOWN("unknown", -1L, 0),
  SHA1("SHA-1", 0L, -1),
  SHA256("SHA-256", 1L, -1);

  private final String digestAlgorithm;
  private final int digestLength;
  /**
   * Constant and unique value as from {@code codezchunk_format.txt}.
   */
  private final long identifier;

  HeaderChecksumType(final String digestAlgorithm, final long identifier, final int manualDigestLength) {
    try {
      this.digestAlgorithm = digestAlgorithm;
      if (manualDigestLength <= -1) {
        // use default
        this.digestLength = MessageDigest.getInstance(digestAlgorithm).getDigestLength();
      } else {
        this.digestLength = manualDigestLength;
      }

      this.identifier = identifier;
    } catch (final NoSuchAlgorithmException algoEx) {
      throw new IllegalArgumentException("Unable to create hashing algorithm: [" + digestAlgorithm + "]. Check your JVM settings.", algoEx);
    }
  }

  public static HeaderChecksumType find(final BigInteger unsignedLongValue) {
    if (unsignedLongValue.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) >= 1) {
      final String message = String.format("Unknown Checksum type: [%s], exeeds [%d]!", unsignedLongValue.toString(), Integer.MAX_VALUE);
      Logger.getLogger(HeaderChecksumType.class.getCanonicalName()).warning(message);
      return UNKNOWN;
    }

    final long requestedId = unsignedLongValue.longValue();

    return Arrays.stream(values())
        .filter(algo -> algo.identifier == requestedId)
        .findFirst()
        .orElse(UNKNOWN);
  }

  public int getDigestLength() {
    return this.digestLength;
  }

  public long getIdentifier() {
    return this.identifier;
  }

  public MessageDigest getMessageDigest() {
    try {
      return MessageDigest.getInstance(this.digestAlgorithm);
    } catch (final NoSuchAlgorithmException algoEx) {
      throw new UnsupportedOperationException("Not implemented: [" + this.digestAlgorithm + "].");
    }
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", HeaderChecksumType.class.getSimpleName() + "[", "]")
        .add("digestAlgorithm=" + this.digestAlgorithm)
        .add("digestLength=" + this.digestLength)
        .add("identifier=" + this.identifier)
        .add("ordinal=" + this.ordinal())
        .toString();
  }

}
