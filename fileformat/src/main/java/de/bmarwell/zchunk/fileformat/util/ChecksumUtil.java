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

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import de.bmarwell.zchunk.fileformat.HeaderChecksumType;
import de.bmarwell.zchunk.fileformat.ZChunkHeader;
import de.bmarwell.zchunk.fileformat.ZChunkHeaderChunkInfo;
import de.bmarwell.zchunk.fileformat.ZChunkHeaderIndex;
import de.bmarwell.zchunk.fileformat.ZChunkHeaderLead;
import de.bmarwell.zchunk.fileformat.ZChunkHeaderPreface;
import de.bmarwell.zchunk.fileformat.ZChunkHeaderSignatures;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public final class ChecksumUtil {

  private static final Logger LOG = Logger.getLogger(ChecksumUtil.class.getCanonicalName());

  private ChecksumUtil() {
    // util
  }

  public static boolean isValid(final ZChunkHeader header) {
    final byte[] expectedChecksum = header.getLead().getChecksum();
    final byte[] calculatedChecksum = calculateHeaderChecksum(header);

    return Arrays.equals(expectedChecksum, calculatedChecksum);
  }

  public static byte[] calculateHeaderChecksum(final ZChunkHeader header) {
    final HeaderChecksumType digestAlgorithm = header.getLead().getChecksumType();
    final MessageDigest digest = digestAlgorithm.digest();

    digest.update(getLeadBytes(header.getLead()));
    digest.update(getPrefaceBytes(header.getPreface()));
    digest.update(getIndexBytes(header.getIndex()));
    digest.update(getSignatureBytes(header.getSignatures()));

    return digest.digest();
  }

  public static byte[] getHeaderWithoutChecksum(final ZChunkHeader header) {
    final ZChunkHeaderLead lead = header.getLead();
    final byte[] leadBytes = getLeadBytes(lead);

    final ZChunkHeaderPreface preface = header.getPreface();
    final byte[] prefaceBytes = getPrefaceBytes(preface);

    final ZChunkHeaderIndex index = header.getIndex();
    final byte[] indexBytes = getIndexBytes(index);

    final byte[] sigs = getSignatureBytes(header.getSignatures());

    return concat(leadBytes, prefaceBytes, indexBytes, sigs);
  }

  private static byte[] getSignatureBytes(final ZChunkHeaderSignatures signatures) {
    return concat(
        signatures.getSignatureCount().getCompressedBytes()
    );
  }

  private static byte[] getIndexBytes(final ZChunkHeaderIndex index) {
    final byte[] chunkInfos = getChunkInfoBytes(index.getChunkInfo());

    final byte[] indexBytes = concat(
        index.getIndexSize().getCompressedBytes(),
        index.getChunkChecksumTypeInt().getCompressedBytes(),
        index.getChunkCount().getCompressedBytes(),
        index.getDictStream().orElse(new byte[0]),
        index.getDictChecksum(),
        index.getDictLength().getCompressedBytes(),
        index.getUncompressedDictLength().getCompressedBytes(),
        chunkInfos
    );

    return indexBytes;
  }

  private static byte[] getChunkInfoBytes(final List<ZChunkHeaderChunkInfo> chunkInfos) {
    final List<byte[]> collect = chunkInfos.stream()
        .map(ChecksumUtil::getChunkInfoBytes)
        .collect(toList());

    return concatByteArrays(collect);
  }

  private static byte[] getChunkInfoBytes(final ZChunkHeaderChunkInfo info) {
    return concat(
        info.getChunkStream().orElse(new byte[0]),
        info.getChunkChecksum(),
        info.getChunkLength().getCompressedBytes(),
        info.getChunkUncompressedLength().getCompressedBytes()
    );
  }


  private static byte[] getPrefaceBytes(final ZChunkHeaderPreface preface) {
    final byte[] prefaceBytes = concat(
        preface.getTotalDataChecksum(),
        preface.getPrefaceFlagsInt().getCompressedBytes(),
        preface.getCompressionAlgorithm().getCompressionTypeValue().getCompressedBytes()
    );

    if (preface.hasOptionalElements()) {
      final byte[] prefaceWithOptional = concat(
          prefaceBytes,
          preface.getOptionalElementCount().getCompressedBytes()
          // TODO: optional elements header
      );

      return prefaceWithOptional;
    }

    return prefaceBytes;
  }

  private static byte[] getLeadBytes(final ZChunkHeaderLead lead) {
    final byte[] leadBytes = concat(
        lead.getId(),
        lead.getChecksumTypeInt().getCompressedBytes(),
        lead.getHeaderSize().getCompressedBytes()
    );

    return leadBytes;
  }

  private static byte[] concat(final byte[]... bytes) {
    final List<byte[]> bytes1 = asList(bytes);

    return concatByteArrays(bytes1);
  }

  private static byte[] concatByteArrays(final List<byte[]> byteList) {
    final int totalLength = byteList.stream()
        .mapToInt(by -> by.length)
        .sum();

    final AtomicInteger targetOffset = new AtomicInteger();
    final byte[] target = new byte[totalLength];

    byteList.forEach(by -> targetOffset.getAndUpdate(off -> {
      System.arraycopy(by, 0, target, off, by.length);
      return off + by.length;
    }));
    return target;
  }


}
