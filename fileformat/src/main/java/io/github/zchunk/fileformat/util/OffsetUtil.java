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

package io.github.zchunk.fileformat.util;

import io.github.zchunk.compressedint.CompressedInt;
import io.github.zchunk.fileformat.OptionalElement;
import io.github.zchunk.fileformat.ZChunkHeader;
import io.github.zchunk.fileformat.ZChunkHeaderChunkInfo;
import io.github.zchunk.fileformat.ZChunkHeaderIndex;
import io.github.zchunk.fileformat.ZChunkHeaderLead;
import io.github.zchunk.fileformat.ZChunkHeaderPreface;
import java.math.BigInteger;
import java.util.SortedSet;

public final class OffsetUtil {

  private OffsetUtil() {
    // util.
  }

  /**
   * The length of the lead. After the lead, the prefix will begin (2nd part of the header).
   *
   * @return the lead length (id/magic length + checksum ci length + headersize ci length + cksum length).
   */
  public static int getLeadLength(final ZChunkHeaderLead lead) {
    return BigInteger.valueOf(lead.getId().length)
        .add(BigInteger.valueOf(lead.getChecksumTypeInt().getCompressedBytes().length))
        .add(BigInteger.valueOf(lead.getHeaderSize().getCompressedBytes().length))
        .add(BigInteger.valueOf(lead.getChecksum().length))
        .intValueExact();
  }

  public static int getTotalHeaderSize(final ZChunkHeaderLead lead) {
    return getLeadLength(lead) + lead.getHeaderSize().getIntValue();
  }

  public static long getPrefaceLength(final ZChunkHeaderPreface preface) {
    /*
     * optional element count only exists if the flag is set.
     */
    final long optElementCountBytes = getOptElementCountBytes(preface);
    final BigInteger totalDataCksumLength = BigInteger.valueOf(preface.getTotalDataChecksum().length);
    final BigInteger prefaceFlagsLength = BigInteger.valueOf(preface.getPrefaceFlagsInt().getCompressedBytes().length);
    final BigInteger compressionTypeLength = BigInteger
        .valueOf(preface.getCompressionAlgorithm().getCompressionTypeValue().getCompressedBytes().length);

    return totalDataCksumLength
        // plus highest preface flag
        .add(prefaceFlagsLength)
        // plus length of compression type
        .add(compressionTypeLength)
        // this might even be 0 if the flag was not set.
        .add(BigInteger.valueOf(optElementCountBytes))
        .add(BigInteger.valueOf(
            preface.getOptionalElements().stream()
                .mapToLong(OptionalElement::getTotalLength)
                .sum()
        ))
        .longValueExact();
  }

  private static long getOptElementCountBytes(final ZChunkHeaderPreface preface) {
    if (preface.getOptionalElementCount().getLongValue() == 0L) {
      return 0L;
    }

    return preface.getOptionalElementCount().getCompressedBytes().length;
  }

  public static long getChunkOffset(final ZChunkHeader zChunkHeader, final long chunkId) {
    final long totalHeaderSize = OffsetUtil.getTotalHeaderSize(zChunkHeader.getLead());
    final CompressedInt dictLength = zChunkHeader.getIndex().getDictLength();

    final long chunkOffset = zChunkHeader.getIndex().getChunkInfoSortedByIndex().stream()
        .limit(chunkId)
        .map(ZChunkHeaderChunkInfo::getChunkLength)
        .mapToLong(CompressedInt::getLongValue)
        .sum();

    return totalHeaderSize + dictLength.getLongValue() + chunkOffset;
  }

  public static long getDictOffset(final ZChunkHeader zChunkHeader) {
    return getTotalHeaderSize(zChunkHeader.getLead());
  }

  public static long getDecompressedChunkOffset(final ZChunkHeaderIndex index, final ZChunkHeaderChunkInfo chunk) {
    final SortedSet<ZChunkHeaderChunkInfo> chunks = index.getChunkInfoSortedByIndex();
    return chunks.stream().limit(chunk.getCurrentIndex())
        .map(ZChunkHeaderChunkInfo::getChunkUncompressedLength)
        .mapToLong(CompressedInt::getLongValue)
        .sum();
  }
}
