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

package io.github.zchunk.fileformat.parser;

import static java.util.Collections.emptyList;

import io.github.zchunk.compressedint.CompressedInt;
import io.github.zchunk.compressedint.CompressedIntFactory;
import io.github.zchunk.fileformat.ImmutableZChunkHeaderChunkInfo;
import io.github.zchunk.fileformat.IndexChecksumType;
import io.github.zchunk.fileformat.PrefaceFlag;
import io.github.zchunk.fileformat.ZChunkHeaderChunkInfo;
import io.github.zchunk.fileformat.ZChunkHeaderLead;
import io.github.zchunk.fileformat.ZChunkHeaderPreface;
import io.github.zchunk.fileformat.util.OffsetUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ZChunkIndexParser {

  private final byte[] completeHeader;
  private final ZChunkHeaderLead lead;
  private final ZChunkHeaderPreface preface;
  private final long indexStart;
  private long cksumTypeOffset = -1;
  private long chunkCountOffset = -1;
  private long optionalDictStreamOffset = -1L;
  private long dictChecksumOffest = -1L;
  private long dictLengthOffset = -1L;
  private long dictUncompressedLengthOffset = -1L;
  private long chunkStreamOffset = -1L;
  private @Nullable CompressedInt chunkCount;
  private @Nullable IndexChecksumType chunkChecksumType;

  private ZChunkIndexParser(final byte[] completeHeader, final ZChunkHeaderLead lead, final ZChunkHeaderPreface preface) {
    this.completeHeader = completeHeader;
    this.lead = lead;
    this.preface = preface;
    final int leadLength = OffsetUtil.getLeadLength(lead);
    final long prefaceLength = OffsetUtil.getPrefaceLength(preface);
    this.indexStart = leadLength + prefaceLength;
  }

  public static ZChunkIndexParser fromBytes(final byte[] completeHeader, final ZChunkHeaderLead lead, final ZChunkHeaderPreface preface) {
    return new ZChunkIndexParser(completeHeader, lead, preface);
  }

  public CompressedInt readIndexSize() {
    try (final ByteArrayInputStream bis = new ByteArrayInputStream(this.completeHeader)) {
      bis.skip(this.indexStart);
      final CompressedInt compressedInt = CompressedIntFactory.readCompressedInt(bis);
      this.cksumTypeOffset = this.indexStart + compressedInt.getCompressedBytes().length;

      return compressedInt;
    } catch (final IOException ioEx) {
      throw new IllegalArgumentException("Cannot read compressed int at offset [" + this.indexStart + "]!", ioEx);
    }
  }

  @EnsuresNonNull("chunkChecksumType")
  public CompressedInt readIndexCksumType() {
    if (this.cksumTypeOffset == -1L) {
      readIndexSize();
    }

    try (final ByteArrayInputStream bis = new ByteArrayInputStream(this.completeHeader)) {
      bis.skip(this.cksumTypeOffset);
      final CompressedInt compressedInt = CompressedIntFactory.readCompressedInt(bis);
      this.chunkCountOffset = this.cksumTypeOffset + compressedInt.getCompressedBytes().length;
      this.chunkChecksumType = IndexChecksumType.find(compressedInt.getValue());

      if (this.chunkChecksumType.equals(IndexChecksumType.UNKNOWN)) {
        throw new IllegalArgumentException("Cannot find index type.");
      }

      return compressedInt;
    } catch (final IOException ioEx) {
      throw new IllegalArgumentException("Cannot read compressed int at offset [" + this.cksumTypeOffset + "]!", ioEx);
    }
  }

  @EnsuresNonNull("chunkCount")
  public CompressedInt readChunkCount() {
    if (this.chunkCountOffset == -1) {
      readIndexCksumType();
    }

    try (final ByteArrayInputStream bis = new ByteArrayInputStream(this.completeHeader)) {
      bis.skip(this.chunkCountOffset);
      final CompressedInt compressedInt = CompressedIntFactory.readCompressedInt(bis);
      this.optionalDictStreamOffset = this.chunkCountOffset + compressedInt.getCompressedBytes().length;
      this.chunkCount = compressedInt;

      return compressedInt;
    } catch (final IOException ioEx) {
      throw new IllegalArgumentException("Cannot read compressed int at offset [" + this.chunkCountOffset + "]!", ioEx);
    }
  }

  public byte[] readDictStream() {
    if (this.optionalDictStreamOffset == -1L) {
      readChunkCount();
    }

    if (!this.preface.getPrefaceFlags().contains(PrefaceFlag.HAS_DATA_STREAMS)) {
      this.dictChecksumOffest = this.optionalDictStreamOffset;
      return new byte[0];
    }

    // TODO: implement property.
    this.dictChecksumOffest = this.optionalDictStreamOffset;
    return new byte[0];
  }

  public byte[] readDictChecksum() {
    if (this.dictChecksumOffest == -1L || null == this.chunkChecksumType) {
      readDictStream();
    }

    try (final ByteArrayInputStream bis = new ByteArrayInputStream(this.completeHeader)) {
      bis.skip(this.dictChecksumOffest);

      // safe to assume, as it is never set back to null.
      @SuppressWarnings("nullness")
      @NonNull
      final IndexChecksumType chunkChecksumType = this.chunkChecksumType;
      final int dictChecksumLength = chunkChecksumType.actualChecksumLength();
      final byte[] dictChecksum = new byte[dictChecksumLength];
      bis.read(dictChecksum);

      this.dictLengthOffset = this.dictChecksumOffest + dictChecksumLength;

      return dictChecksum;
    } catch (final IOException ioEx) {
      throw new IllegalArgumentException("Cannot read byte[] int at offset [" + this.dictChecksumOffest + "]!", ioEx);
    }
  }

  public CompressedInt readDictLength() {
    if (this.dictLengthOffset == -1L) {
      readDictChecksum();
    }

    try (final ByteArrayInputStream bis = new ByteArrayInputStream(this.completeHeader)) {
      bis.skip(this.dictLengthOffset);

      final CompressedInt compressedInt = CompressedIntFactory.readCompressedInt(bis);
      this.dictUncompressedLengthOffset = this.dictLengthOffset + compressedInt.getCompressedBytes().length;

      return compressedInt;
    } catch (final IOException ioEx) {
      throw new IllegalArgumentException("Cannot read compressed int at offset [" + this.dictLengthOffset + "]!", ioEx);
    }
  }

  public CompressedInt readUncompressedDictLength() {
    if (this.dictUncompressedLengthOffset == -1L) {
      readDictChecksum();
    }

    try (final ByteArrayInputStream bis = new ByteArrayInputStream(this.completeHeader)) {
      bis.skip(this.dictUncompressedLengthOffset);

      final CompressedInt compressedInt = CompressedIntFactory.readCompressedInt(bis);
      this.chunkStreamOffset = this.dictUncompressedLengthOffset + compressedInt.getCompressedBytes().length;

      return compressedInt;
    } catch (final IOException ioEx) {
      throw new IllegalArgumentException("Cannot read compressed int at offset [" + this.dictLengthOffset + "]!", ioEx);
    }
  }

  public List<? extends ZChunkHeaderChunkInfo> readChunkInfos() {
    if (this.chunkStreamOffset == -1L || null == this.chunkChecksumType || null == this.chunkCount) {
      readUncompressedDictLength();
    }

    // safe to assume, as it is never set back to null.
    @SuppressWarnings("nullness")
    @NonNull
    final IndexChecksumType chunkChecksumType = this.chunkChecksumType;
    // safe to assume, as it is never set back to null.
    @SuppressWarnings("nullness")
    @NonNull
    final CompressedInt chunkCount = this.chunkCount;

    if (chunkCount.getValue().equals(BigInteger.ZERO)) {
      return emptyList();
    }

    final List<ZChunkHeaderChunkInfo> chunkInfo = new ArrayList<>();

    long currentOffset = 0;
    // first chunk is the dict chunk.
    for (long chunkNumber = 0; chunkNumber < chunkCount.getLongValue() - 1L; chunkNumber++) {
      try (final ByteArrayInputStream bis = new ByteArrayInputStream(this.completeHeader)) {
        bis.skip(this.chunkStreamOffset + currentOffset);
        if (this.preface.getPrefaceFlags().contains(PrefaceFlag.HAS_DATA_STREAMS)) {
          //TODO: chunkStream
          throw new UnsupportedOperationException("data streams not implemented.");
        }

        final byte[] chunkChecksum = new byte[chunkChecksumType.actualChecksumLength()];
        bis.read(chunkChecksum);

        final CompressedInt compressedChunkLength = CompressedIntFactory.readCompressedInt(bis);
        final CompressedInt uncompressedChunkLength = CompressedIntFactory.readCompressedInt(bis);

        chunkInfo.add(ImmutableZChunkHeaderChunkInfo.builder()
            .currentIndex(chunkNumber)
            .chunkChecksum(chunkChecksum)
            .chunkLength(compressedChunkLength)
            .chunkUncompressedLength(uncompressedChunkLength)
            .build());

        currentOffset += chunkChecksumType.actualChecksumLength()
            + compressedChunkLength.getCompressedBytes().length
            + uncompressedChunkLength.getCompressedBytes().length;
      } catch (final IOException ioEx) {
        throw new IllegalArgumentException(
            "Cannot read chunk info no. [" + chunkNumber + "] at offset [" + this.chunkStreamOffset + "]!", ioEx);
      }
    }

    return chunkInfo;
  }
}
