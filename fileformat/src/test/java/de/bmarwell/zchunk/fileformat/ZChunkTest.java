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

package de.bmarwell.zchunk.fileformat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ZChunkTest {

  @Test
  public void testZChunk_validate_valid() {
    final boolean validateFile = ZChunk.validateFile(ZChunkFileTest.TEST_FILE);
    Assertions.assertTrue(validateFile);
  }

  @Test
  public void testZChunk_validate_invalid() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> ZChunk.validateFile(ZChunkFileTest.TEST_FILE_INVALID));
  }

  @Test
  public void testZChunk_validate_checksum_header_invalid() {
    final boolean validateFile = ZChunk.validateFile(ZChunkFileTest.TEST_FILE_HEADER_CKSUM_INVALID);
    Assertions.assertFalse(validateFile);
  }

  @Test
  public void testZChunk_validate_digest_invalid() {
    Assertions.assertThrows(
        UnsupportedOperationException.class,
        () -> ZChunk.validateFile(ZChunkFileTest.TEST_FILE_HEADER_DIGEST_INVALID));
  }

}
