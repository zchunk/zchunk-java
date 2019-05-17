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

package de.bmarwell.zchunk.fileformat.parser;

import de.bmarwell.zchunk.compressedint.CompressedInt;
import de.bmarwell.zchunk.compressedint.CompressedIntFactory;
import de.bmarwell.zchunk.fileformat.ZChunkHeaderLead;
import de.bmarwell.zchunk.fileformat.util.OffsetUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ZChunkPrefaceParser {

  private final byte[] header;
  private final ZChunkHeaderLead lead;
  private final long flagsOffset;
  private final long prefaceOffset;
  private long compressionTypeOffset = -1L;

  private ZChunkPrefaceParser(final byte[] completeHeader, final ZChunkHeaderLead lead) {
    this.header = completeHeader;
    this.lead = lead;
    this.prefaceOffset = OffsetUtil.getLeadLength(lead);
    this.flagsOffset = this.prefaceOffset + lead.getChecksumType().getDigestLength();
  }

  public static ZChunkPrefaceParser fromBytes(final byte[] completeHeader, final ZChunkHeaderLead lead) {
    if (completeHeader.length < OffsetUtil.getTotalHeaderSize(lead)) {
      throw new IllegalArgumentException("Byte array to short!");
    }

    return new ZChunkPrefaceParser(completeHeader, lead);
  }

  public byte[] readTotalDataCksum() {
    final int digestLength = this.lead.getChecksumType().getDigestLength();

    try (final ByteArrayInputStream bis = new ByteArrayInputStream(this.header)) {
      final long skip = bis.skip(this.prefaceOffset);
      final byte[] cksum = new byte[digestLength];
      final int read = bis.read(cksum, 0, digestLength);

      if (read < digestLength) {
        throw new IllegalArgumentException("Cannot read cksum of length [" + digestLength + "] at offset [" + this.prefaceOffset + "]!");
      }

      return cksum;
    } catch (final IOException ioEx) {
      throw new IllegalArgumentException("Cannot read cksum of length [" + digestLength + "] at offset [" + this.prefaceOffset + "]!",
          ioEx);
    }
  }

  public CompressedInt readFlagsInt() {
    try (final ByteArrayInputStream bis = new ByteArrayInputStream(this.header)) {
      bis.skip(this.flagsOffset);
      final CompressedInt compressedInt = CompressedIntFactory.readCompressedInt(bis);
      this.compressionTypeOffset = this.flagsOffset + compressedInt.getCompressedBytes().length;

      return compressedInt;
    } catch (final IOException ioEx) {
      throw new IllegalArgumentException("Cannot read compressed int at offset [" + this.flagsOffset + "]!", ioEx);
    }
  }

  public CompressedInt readCompressionType() {
    if (this.compressionTypeOffset == -1L) {
      readFlagsInt();
    }

    try (final ByteArrayInputStream bis = new ByteArrayInputStream(this.header)) {
      bis.skip(this.compressionTypeOffset);
      final CompressedInt compressedInt = CompressedIntFactory.readCompressedInt(bis);

      return compressedInt;
    } catch (final IOException ioEx) {
      throw new IllegalArgumentException("Cannot read compressed int at offset [" + this.compressionTypeOffset + "]!", ioEx);
    }
  }

}
