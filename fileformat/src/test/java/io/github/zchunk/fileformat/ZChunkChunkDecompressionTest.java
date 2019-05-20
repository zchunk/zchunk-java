/*
 *  Copyright 2018 The zchunk-java contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.zchunk.fileformat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ZChunkChunkDecompressionTest {

  public static final File TEST_FILE = new File(
      ZChunkChunkDecompressionTest.class.getResource("/testfiles/LICENSE.dict.fodt.zck").getPath());

  @Test
  public void testDecompressDictOnFirstChunk() {
    final ZChunkFile zChunkFile = ZChunk.fromFile(TEST_FILE);
    final ZChunkHeader header = zChunkFile.getHeader();

    final byte[] dict = ZChunk.getDecompressedDict(header, TEST_FILE);
    final ZChunkHeaderChunkInfo chunkInfo = ZChunk.getChunkInfo(header, 0L);
    final byte[] chunkBuffer = new byte[chunkInfo.getChunkUncompressedLength().getIntValue()];

    try (final InputStream is = ZChunk.getDecompressedChunk(header, TEST_FILE, dict, 0L)) {
      // only read the first few bytes to see if it works.
      is.read(chunkBuffer, 0, 120);
    } catch (final IOException ioEx) {
      throw new RuntimeException(ioEx);
    }

    final String readBytes = new String(chunkBuffer, StandardCharsets.UTF_8);
    Assertions.assertTrue(readBytes.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
  }

}
