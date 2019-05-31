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

import io.github.zchunk.compression.api.CompressionAlgorithm;
import io.github.zchunk.fileformat.err.InvalidFileException;
import io.github.zchunk.fileformat.io.BoundedInputStream;
import io.github.zchunk.fileformat.util.ChecksumUtil;
import io.github.zchunk.fileformat.util.OffsetUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiFunction;
import java.util.logging.Logger;

public final class ZChunk {

  private static final Logger LOG = Logger.getLogger("io.github.zchunk.fileformat.ZChunk");

  /**
   * Reads in a zchunk file.
   *
   * <p>The header part will stay in memory (heap).<br>
   * The data streams and/or chunks will be available as inputstream, but are not
   * eagerly loaded into memory.</p>
   *
   * @param input
   *     the input file.
   * @return a {@link ZChunkFile} instance.
   * @throws InvalidFileException
   *     if the input file is not a zchunk file.
   * @throws NullPointerException
   *     if the input file is {@code null}.
   */
  public static ZChunkFile fromFile(final File input) {
    final ZChunkHeader header = ZChunkHeaderFactory.getZChunkFileHeader(input);

    return ImmutableZChunkFile.builder().header(header).build();
  }

  public static boolean validateFile(final File file) {
    final ZChunkFile zChunkFile = fromFile(file);
    final ZChunkHeader header = zChunkFile.getHeader();

    return ChecksumUtil.isValidHeader(header)
        && ChecksumUtil.allChunksAreValid(header, file)
        && ChecksumUtil.isValidData(header, file);
  }

  /**
   * Get a chunk info item.
   *
   * @param header
   *     the header to extract the info from.
   * @param chunkNumber
   *     the chunk number to extract.
   * @return the ZChunk if it was found.
   * @throws IllegalArgumentException
   *     if the chunk was not found.
   */
  public static ZChunkHeaderChunkInfo getChunkInfo(final ZChunkHeader header, final long chunkNumber) {
    return header.getIndex().getChunkInfoSortedByIndex().stream()
        .filter(currChunk -> currChunk.getCurrentIndex() == chunkNumber)
        .findFirst().orElseThrow(IllegalArgumentException::new);
  }

  public static byte[] getDecompressedDict(final ZChunkHeader header, final File input) {
    final long offset = OffsetUtil.getDictOffset(header);
    final CompressionAlgorithm compressionAlgorithm = header.getPreface().getCompressionAlgorithm();
    final BiFunction<InputStream, byte[], InputStream> decompressor = compressionAlgorithm.getOutputStreamSupplier();

    try (
        final FileInputStream fis = new FileInputStream(input);
        final InputStream decompressedStream = decompressor.apply(fis, new byte[0])
    ) {
      fis.skip(offset);
      final byte[] dictBuffer = new byte[header.getIndex().getUncompressedDictLength().getIntValue()];
      decompressedStream.read(dictBuffer);
      return dictBuffer;
    } catch (final IOException ioEx) {
      final String message = String.format("Unable to read dictionary at offset [%d] from file [%s].", offset, input.getAbsolutePath());
      throw new IllegalArgumentException(message);
    }
  }

  public static InputStream getDecompressedDictStream(final ZChunkHeader header, final File input) {
    final long offset = OffsetUtil.getDictOffset(header);
    final CompressionAlgorithm compressionAlgorithm = header.getPreface().getCompressionAlgorithm();
    final BiFunction<InputStream, byte[], InputStream> decompressor = compressionAlgorithm.getOutputStreamSupplier();

    try {
      final FileInputStream fis = new FileInputStream(input);
      final InputStream decompressedStream = decompressor.apply(fis, new byte[0]);
      fis.skip(offset);

      return new BoundedInputStream(decompressedStream, header.getIndex().getUncompressedDictLength().getIntValue());
    } catch (final IOException ioEx) {
      final String message = String.format("Unable to read dictionary at offset [%d] from file [%s].", offset, input.getAbsolutePath());
      throw new IllegalArgumentException(message);
    }
  }

  public static InputStream getDecompressedChunk(final ZChunkHeader header,
                                                 final File testFile,
                                                 final byte[] dict,
                                                 final long chunkNumber) throws IOException {
    final long chunkOffset = OffsetUtil.getChunkOffset(header, chunkNumber);
    final ZChunkHeaderChunkInfo chunk = getChunkInfo(header, chunkNumber);
    final CompressionAlgorithm compressionAlgorithm = header.getPreface().getCompressionAlgorithm();
    final BiFunction<InputStream, byte[], InputStream> decompressor = compressionAlgorithm.getOutputStreamSupplier();

    // including skip
    final long compressedBytesReadLimit = chunkOffset + chunk.getChunkLength().getLongValue();
    final BoundedInputStream fis = new BoundedInputStream(new FileInputStream(testFile), compressedBytesReadLimit);
    fis.skip(chunkOffset);

    return decompressor.apply(fis, dict);
  }
}
