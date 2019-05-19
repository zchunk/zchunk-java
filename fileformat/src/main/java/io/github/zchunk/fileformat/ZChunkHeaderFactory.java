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

import static io.github.zchunk.fileformat.ZChunkConstants.Header.MAX_LEAD_SIZE;
import static java.util.stream.Collectors.toConcurrentMap;

import io.github.zchunk.compressedint.CompressedInt;
import io.github.zchunk.compressedint.CompressedIntFactory;
import io.github.zchunk.compression.api.CompressionAlgorithm;
import io.github.zchunk.compression.api.CompressionAlgorithmFactory;
import io.github.zchunk.compression.api.ImmutableCompressionAlgorithm;
import io.github.zchunk.fileformat.err.InvalidFileException;
import io.github.zchunk.fileformat.parser.ZChunkIndexParser;
import io.github.zchunk.fileformat.parser.ZChunkLeadParser;
import io.github.zchunk.fileformat.parser.ZChunkPrefaceParser;
import io.github.zchunk.fileformat.util.OffsetUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.channels.Channels;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;

public final class ZChunkHeaderFactory {

  private static final Logger LOG = Logger.getLogger(ZChunkHeaderFactory.class.getCanonicalName());

  private ZChunkHeaderFactory() {
    //
  }

  public static ZChunkHeader getZChunkFileHeader(final File input) {
    final ZChunkHeaderLead lead = readFileHeaderLead(input);
    if (lead.getChecksumType() == HeaderChecksumType.UNKNOWN) {
      throw new UnsupportedOperationException("Unknown getMessageDigest type: [" + lead.getChecksumType() + "].");
    }
    final byte[] completeHeader = readCompleteHeader(input, lead);
    final ZChunkHeaderPreface preface = readHeaderPreface(completeHeader, lead);
    final ZChunkHeaderIndex index = readHeaderIndex(completeHeader, lead, preface);
    final ZChunkHeaderSignatures signatures = readSignatureIndex(completeHeader, lead, preface, index);

    return ImmutableZChunkHeader.builder()
        .lead(lead)
        .preface(preface)
        .index(index)
        .signatures(signatures)
        .build();
  }

  public static ZChunkHeader fromStream(final InputStream byteStream) {
    try {
      final byte[] leadBytes = new byte[MAX_LEAD_SIZE];
      final int read = byteStream.read(leadBytes);

      if (read < MAX_LEAD_SIZE) {
        throw new IllegalArgumentException("Unable to read enough bytes from bytestream!");
      }

      final ZChunkHeaderLead lead = readFileHeaderLead(leadBytes);
      final byte[] completeHeader = new byte[OffsetUtil.getTotalHeaderSize(lead)];
      System.arraycopy(leadBytes, 0, completeHeader, 0, leadBytes.length);
      final long bytesRemaining = OffsetUtil.getLeadLength(lead) - read;

      if (BigInteger.valueOf(bytesRemaining).compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) == 1) {
        throw new IllegalStateException("Cannot read remaining [" + bytesRemaining + "] bytes. Value too large for integer.");
      }

      final byte[] headerBytesRemaining = new byte[Math.toIntExact(bytesRemaining)];
      byteStream.read(headerBytesRemaining);
      System.arraycopy(headerBytesRemaining, 0, completeHeader, leadBytes.length, headerBytesRemaining.length);

      final ZChunkHeaderPreface headerPreface = readHeaderPreface(completeHeader, lead);

      final ZChunkHeaderIndex index = readHeaderIndex(completeHeader, lead, headerPreface);
      final ZChunkHeaderSignatures signature = ImmutableZChunkHeaderSignatures.builder()
          .signatureCount(CompressedIntFactory.valueOf(0L))
          .build();

      return ImmutableZChunkHeader.builder()
          .lead(lead)
          .preface(headerPreface)
          .index(index)
          .signatures(signature)
          .build();
    } catch (final IOException ioEx) {
      throw new IllegalArgumentException("Unable to read enough bytes from bytestream!", ioEx);
    }
  }

  public static ZChunkHeaderLead readFileHeaderLead(final byte[] input) {
    if (input.length < MAX_LEAD_SIZE) {
      throw new IllegalArgumentException("No enough bytes to read lead.");
    }

    final ZChunkLeadParser leadParser = ZChunkLeadParser.fromBytes(input);

    return getZChunkFileHeaderLeadFromParser(leadParser);
  }

  public static ZChunkHeaderLead readFileHeaderLead(final File input) {
    final ZChunkLeadParser leadParser = ZChunkLeadParser.fromFile(input);

    return getZChunkFileHeaderLeadFromParser(leadParser);
  }

  private static ZChunkHeaderLead getZChunkFileHeaderLeadFromParser(final ZChunkLeadParser leadParser) {
    return ImmutableZChunkHeaderLead.builder()
        .id(leadParser.readLeadId())
        .checksumTypeInt(leadParser.readLeadCksumType())
        .headerSize(leadParser.readHeaderSize())
        .checksum(leadParser.readHeaderChecksum())
        .build();
  }

  private static byte[] readCompleteHeader(final File input, final ZChunkHeaderLead lead) {
    try (final FileInputStream fis = new FileInputStream(input)) {
      final int totalHeaderSize = OffsetUtil.getTotalHeaderSize(lead);
      final byte[] buffer = new byte[totalHeaderSize];
      final int readCount = fis.read(buffer);

      if (readCount < totalHeaderSize) {
        throw new IllegalArgumentException("Cannot read header, file too short?");
      }

      return buffer;
    } catch (final IOException ioEx) {
      throw new InvalidFileException("File too short?", input, ioEx);
    }
  }

  public static ZChunkHeaderPreface readHeaderPreface(final byte[] completeHeader, final ZChunkHeaderLead lead) {
    final ZChunkPrefaceParser prefaceParser = ZChunkPrefaceParser.fromBytes(completeHeader, lead);

    return getZChunkFileHeaderPrefaceFromParser(prefaceParser);
  }

  private static ZChunkHeaderPreface getZChunkFileHeaderPrefaceFromParser(final ZChunkPrefaceParser prefaceParser) {
    final CompressedInt prefaceFlagsInt = prefaceParser.readFlagsInt();
    final Set<PrefaceFlag> flags = PrefaceFlag.getPrefaceFlags(prefaceFlagsInt);

    final CompressionAlgorithm compressionAlgorithm = ImmutableCompressionAlgorithm.builder()
        .compressionTypeValue(prefaceParser.readCompressionType())
        .name("unknown")
        .outputStreamSupplier(a -> a)
        .build();

    return ImmutableZChunkHeaderPreface.builder()
        .totalDataChecksum(prefaceParser.readTotalDataCksum())
        .prefaceFlagsInt(prefaceFlagsInt)
        .compressionAlgorithm(compressionAlgorithm)
        .optionalElementCount(CompressedIntFactory.valueOf(0))
        .addAllPrefaceFlags(flags)
        .build();
  }

  public static ZChunkHeaderPreface readFileHeaderPreface(final File input, final ZChunkHeaderLead lead) {
    return readFileHeaderPreface(input, lead.getChecksumType(), OffsetUtil.getLeadLength(lead));
  }

  public static ZChunkHeaderPreface readFileHeaderPreface(final File zckFile, final HeaderChecksumType headerChecksumType,
      final long leadLength) {
    final byte[] cksum = new byte[headerChecksumType.getDigestLength()];

    try (final RandomAccessFile randomAccessFile = new RandomAccessFile(zckFile, "r")) {
      randomAccessFile.seek(leadLength);
      randomAccessFile.read(cksum);

      final InputStream inputStream = Channels.newInputStream(randomAccessFile.getChannel());
      final CompressedInt flags = getPrefaceFlagsFromInputStream(inputStream);

      final CompressedInt compressionType = CompressedIntFactory.readCompressedInt(inputStream);
      final CompressionAlgorithm compressionAlgorithm = CompressionAlgorithmFactory.forType(compressionType.getLongValue());

      return ImmutableZChunkHeaderPreface.builder()
          .totalDataChecksum(cksum)
          .prefaceFlagsInt(flags)
          .compressionAlgorithm(compressionAlgorithm)
          .optionalElementCount(CompressedIntFactory.valueOf(0))
          .addAllPrefaceFlags(PrefaceFlag.getPrefaceFlags(flags))
          .build();
    } catch (final IOException ioEx) {
      throw new InvalidFileException("Unable to read preface of file.", zckFile, ioEx);
    }
  }

  public static CompressedInt getPrefaceFlagsFromInputStream(final InputStream inputStream) throws IOException {
    return CompressedIntFactory.readCompressedInt(inputStream);
  }

  private static ZChunkHeaderIndex readHeaderIndex(final byte[] completeHeader, final ZChunkHeaderLead lead,
      final ZChunkHeaderPreface preface) {
    final ZChunkIndexParser parser = ZChunkIndexParser.fromBytes(completeHeader, lead, preface);

    final CompressedInt indexChecksumType = parser.readIndexCksumType();

    return ImmutableZChunkHeaderIndex.builder()
        .indexSize(parser.readIndexSize())
        .chunkChecksumTypeInt(indexChecksumType)
        .chunkCount(parser.readChunkCount())
        .dictChecksum(parser.readDictChecksum())
        .dictLength(parser.readDictLength())
        .uncompressedDictLength(parser.readUncompressedDictLength())
        .putAllChunkInfo(parser.readChunkInfos().stream()
            .collect(toConcurrentMap(ZChunkHeaderChunkInfo::getChunkChecksum, Function.identity())))
        .build();
  }

  private static ZChunkHeaderSignatures readSignatureIndex(
      final byte[] completeHeader, final ZChunkHeaderLead lead, final ZChunkHeaderPreface preface, final ZChunkHeaderIndex index) {

    return ImmutableZChunkHeaderSignatures.builder()
        .signatureCount(CompressedIntFactory.valueOf(0L))
        .build();
  }

}
