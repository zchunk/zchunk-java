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

import static io.github.zchunk.fileformat.ZChunkConstants.Header.FILE_MAGIC;
import static io.github.zchunk.fileformat.ZChunkConstants.Header.MAX_LEAD_SIZE;

import io.github.zchunk.compressedint.CompressedInt;
import io.github.zchunk.compressedint.CompressedIntFactory;
import io.github.zchunk.fileformat.HeaderChecksumType;
import io.github.zchunk.fileformat.err.InvalidFileException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ZChunkLeadParser {

  private final byte[] leadBytes = new byte[MAX_LEAD_SIZE];
  private @Nullable HeaderChecksumType checksumType;
  private long headerSizeOffset;
  private long headerChecksumOffset;

  /**
   * Private constructor because of possible unsafe operations.
   *
   * @param leadBytes
   *     the bytes belonging to the lead.
   */
  private ZChunkLeadParser(final byte[] leadBytes) {
    final int length = leadBytes.length;

    if (length < MAX_LEAD_SIZE) {
      final String message = String.format("lead bytes are too short with [%d] bytes. Need at least [%d] bytes.", length, MAX_LEAD_SIZE);
      throw new IllegalArgumentException(message);
    }

    System.arraycopy(leadBytes, 0, this.leadBytes, 0, MAX_LEAD_SIZE);
  }

  /**
   * Create a parser from the given bytes.
   *
   * @param leadBytes
   *     the bytes to take as a lead.
   * @return a parser instance.
   */
  public static ZChunkLeadParser fromBytes(final byte[] leadBytes) {
    return new ZChunkLeadParser(leadBytes);
  }

  public static ZChunkLeadParser fromFile(final File input) {
    try (final FileInputStream fr = new FileInputStream(input)) {
      final byte[] buffer = new byte[MAX_LEAD_SIZE];
      final int read = fr.read(buffer);

      if (read < MAX_LEAD_SIZE) {
        throw new InvalidFileException(getExceptionMessage(input), input);
      }

      if (read != buffer.length) {
        throw new InvalidFileException(getExceptionMessage(input), input);
      }

      return new ZChunkLeadParser(buffer);
    } catch (final IOException ioEx) {
      throw new InvalidFileException(getExceptionMessage(input), input, ioEx);
    }
  }

  private static String getExceptionMessage(final File input) {
    return String.format("Unable to read [%d] bytes from file [%s].", MAX_LEAD_SIZE, input.getAbsolutePath());
  }

  public byte[] readLeadId() {
    final int length = FILE_MAGIC.length;

    try (final ByteArrayInputStream bis = new ByteArrayInputStream(this.leadBytes)) {
      final byte[] leadid = new byte[length];
      bis.read(leadid);

      return leadid;
    } catch (final IOException e) {
      throw new IllegalArgumentException("Not a zchunk lead.");
    }
  }

  @EnsuresNonNull("checksumType")
  public CompressedInt readLeadCksumType() {
    try (final ByteArrayInputStream bis = new ByteArrayInputStream(this.leadBytes)) {
      final long skip = bis.skip(FILE_MAGIC.length);
      if (skip < FILE_MAGIC.length) {
        throw new IllegalStateException("Unable to skip [" + FILE_MAGIC.length + "] bytes!");
      }

      final CompressedInt checksumType = CompressedIntFactory.readCompressedInt(bis);
      this.headerSizeOffset = FILE_MAGIC.length + (long) checksumType.getCompressedBytes().length;
      this.checksumType = HeaderChecksumType.find(checksumType.getValue());

      return checksumType;
    } catch (final IOException ioEx) {
      throw new IllegalArgumentException("Cannot read compressed int at offset [" + FILE_MAGIC.length + "]!", ioEx);
    }
  }

  public CompressedInt readHeaderSize() {
    if (this.headerSizeOffset == -1L || null == this.checksumType) {
      readLeadCksumType();
    }

    try (final ByteArrayInputStream bis = new ByteArrayInputStream(this.leadBytes)) {
      bis.skip(this.headerSizeOffset);
      final CompressedInt compressedInt = CompressedIntFactory.readCompressedInt(bis);
      this.headerChecksumOffset = this.headerSizeOffset + compressedInt.getCompressedBytes().length;

      return compressedInt;
    } catch (final IOException ioEx) {
      throw new IllegalArgumentException("Cannot read compressed int at offset [" + this.headerSizeOffset + "]!", ioEx);
    }
  }

  public byte[] readHeaderChecksum() {
    if (this.headerChecksumOffset == -1L || null == this.checksumType) {
      readHeaderSize();
    }

    // as checksumType is never set back to null, this is safe to assume.
    @SuppressWarnings("nullness")
    @NonNull
    final HeaderChecksumType checksumType = this.checksumType;
    final int cksumLength = checksumType.getDigestLength();

    try (final ByteArrayInputStream bis = new ByteArrayInputStream(this.leadBytes)) {
      bis.skip(this.headerChecksumOffset);
      final byte[] buffer = new byte[cksumLength];
      final int readBytes = bis.read(buffer);

      if (readBytes < cksumLength) {
        throw new IllegalStateException("Unable to read [" + cksumLength + "] bytes for checksum!");
      }

      return buffer;
    } catch (final IOException ioEx) {
      throw new IllegalArgumentException("Cannot read cksum of length [" + cksumLength + "] at offset [" + this.headerChecksumOffset + "]!",
          ioEx);
    }
  }
}
