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

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import io.github.zchunk.fileformat.HeaderChecksumType;
import io.github.zchunk.fileformat.PrefaceFlag;
import io.github.zchunk.fileformat.ZChunkHeader;
import io.github.zchunk.fileformat.ZChunkHeaderChunkInfo;
import io.github.zchunk.fileformat.ZChunkHeaderIndex;
import io.github.zchunk.fileformat.ZChunkHeaderLead;
import io.github.zchunk.fileformat.ZChunkHeaderPreface;
import io.github.zchunk.fileformat.ZChunkHeaderSignatures;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ChecksumUtil {

  private static final Logger LOG = Logger.getLogger(ChecksumUtil.class.getCanonicalName());

  /**
   * Buffer size for reading files.
   */
  private static final int BUFFER_SIZE = 1024;

  private ChecksumUtil() {
    // util
  }

  public static boolean isValidHeader(final ZChunkHeader header) {
    final byte[] expectedChecksum = header.getLead().getChecksum();
    final byte[] calculatedChecksum = calculateHeaderChecksum(header);

    return Arrays.equals(expectedChecksum, calculatedChecksum);
  }

  public static byte[] calculateHeaderChecksum(final ZChunkHeader header) {
    final HeaderChecksumType digestAlgorithm = header.getLead().getChecksumType();
    final MessageDigest digest = digestAlgorithm.getMessageDigest();

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

    final byte[] chunkInfos = getChunkInfoBytes(index.getChunkInfoSortedByIndex());

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

  private static byte[] getChunkInfoBytes(final Collection<ZChunkHeaderChunkInfo> chunkInfos) {
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
      // TODO: optional elements header
      final byte[] prefaceWithOptional = concat(
          prefaceBytes,
          preface.getOptionalElementCount().getCompressedBytes()
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


  public static boolean isValidData(final ZChunkHeader zChunkHeader, final File fileToCheck) {
    if (zChunkHeader.getPreface().getPrefaceFlags().contains(PrefaceFlag.HAS_DATA_STREAMS)) {
      throw new UnsupportedOperationException("Data streams not supported yet.");
    }

    final int totalHeaderSize = OffsetUtil.getTotalHeaderSize(zChunkHeader.getLead());
    final HeaderChecksumType chunkChecksumType = zChunkHeader.getLead().getChecksumType();
    final MessageDigest messageDigest = chunkChecksumType.getMessageDigest();

    try (final FileInputStream fis = new FileInputStream(fileToCheck)) {
      fis.skip(totalHeaderSize);
      final byte[] buffer = new byte[BUFFER_SIZE];
      int read = 0;
      while ((read = fis.read(buffer)) == BUFFER_SIZE) {
        messageDigest.update(buffer);
      }
      final byte[] lastChunk = new byte[read];
      System.arraycopy(buffer, 0, lastChunk, 0, read);
      messageDigest.update(lastChunk);

      final byte[] expected = zChunkHeader.getPreface().getTotalDataChecksum();
      final byte[] actual = messageDigest.digest();

      return Arrays.equals(expected, actual);
    } catch (final IOException ioEx) {
      LOG.log(Level.SEVERE, ioEx, () -> "Unable to seek [" + totalHeaderSize + "] bytes into the file.");
      return false;
    }

  }

  public static boolean allChunksAreValid(final ZChunkHeader zchunkFile, final File file) {
    return zchunkFile.getIndex().getChunkInfoSortedByIndex().stream()
        .allMatch(chunk -> chunkIsValid(chunk, zchunkFile, file));
  }

  private static boolean chunkIsValid(final ZChunkHeaderChunkInfo chunk, final ZChunkHeader zchunkFile, final File file) {
    final long chunkOffset = OffsetUtil.getChunkOffset(zchunkFile, chunk.getCurrentIndex());

    try (final FileInputStream fis = new FileInputStream(file)) {
      fis.skip(chunkOffset);
      final byte[] chunkData = new byte[chunk.getChunkLength().getIntValue()];
      fis.read(chunkData);
      final byte[] digest = zchunkFile.getIndex().getChunkChecksumType().digest(chunkData);

      return Arrays.equals(chunk.getChunkChecksum(), digest);
    } catch (final IOException ioEx) {
      LOG.log(Level.SEVERE, ioEx, () -> "Unable to seek [" + chunkOffset + "] bytes into the file.");
      return false;
    }
  }
}
