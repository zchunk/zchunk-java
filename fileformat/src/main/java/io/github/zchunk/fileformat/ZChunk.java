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

import io.github.zchunk.fileformat.err.InvalidFileException;
import io.github.zchunk.fileformat.util.ChecksumUtil;
import java.io.File;

public class ZChunk {

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

}
