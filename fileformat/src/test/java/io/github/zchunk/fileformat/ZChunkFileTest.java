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

import io.github.zchunk.fileformat.util.ByteUtils;
import io.github.zchunk.fileformat.util.ChecksumUtil;
import io.github.zchunk.fileformat.util.OffsetUtil;
import java.io.File;
import java.util.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("SpellCheckingInspection")
public class ZChunkFileTest {

  private static final Logger LOG = Logger.getLogger(ZChunkFileTest.class.getCanonicalName());

  public static final File TEST_FILE = new File(ZChunkFileTest.class.getResource("/testfiles/LICENSE.dict.fodt.zck").getPath());

  public static final File TEST_FILE_INVALID = new File(
      ZChunkFileTest.class.getResource("/testfiles/LICENSE.dict.fodt.zck.invalid").getPath());
  public static final File TEST_FILE_HEADER_CKSUM_INVALID = new File(
      ZChunkFileTest.class.getResource("/testfiles/LICENSE.dict.fodt.header-cksum-invalid.zck").getPath());

  public static final File TEST_FILE_HEADER_DIGEST_INVALID = new File(
      ZChunkFileTest.class.getResource("/testfiles/LICENSE.dict.fodt.digest-invalid.zck").getPath());

  @Test
  public void testFileFormat() {
    final ZChunkFile zChunkFile = ZChunk.fromFile(TEST_FILE);

    final ZChunkHeader header = zChunkFile.getHeader();

    Assertions.assertAll(
        () -> testLead(header.getLead()),
        () -> testPreface(header.getPreface()),
        () -> testIndex(header.getIndex()),
        () -> checkChecksum(header)
    );
  }

  private void checkChecksum(final ZChunkHeader header) {
    final byte[] exp = header.getLead().getChecksum();
    final byte[] actual = ChecksumUtil.calculateHeaderChecksum(header);

    Assertions.assertAll(
        () -> Assertions.assertArrayEquals(exp, actual),
        () -> Assertions.assertTrue(ChecksumUtil.isValidHeader(header))
    );
  }

  @Test
  public void testInvalidFile() {
    Assertions.assertAll(
        () -> Assertions.assertThrows(IllegalArgumentException.class, () -> ZChunkHeaderFactory.readFileHeaderLead(TEST_FILE_INVALID)),
        () -> Assertions.assertThrows(IllegalArgumentException.class, () -> ZChunk.fromFile(TEST_FILE_INVALID))
    );
  }

  private void testLead(final ZChunkHeaderLead lead) {
    final byte[] expectedCksum = ByteUtils.hexStringToByteArray("f666ca3e0330c42aa4bfbb58ab0576788d720cbbb1af0291f11e256b7bda2e79");

    Assertions.assertAll(
        () -> Assertions.assertArrayEquals(ZChunkConstants.Header.FILE_MAGIC, lead.getId()),
        () -> Assertions.assertEquals(1L, lead.getChecksumType().getIdentifier()),
        () -> Assertions.assertEquals(394L, lead.getHeaderSize().getLongValue()),
        () -> Assertions.assertEquals(394, lead.getHeaderSize().getIntValue()),
        // TODO: 434 (reported by zck_read_header) vs 432 (this implementation).
        () -> Assertions.assertEquals(434L, OffsetUtil.getTotalHeaderSize(lead)),
        () -> Assertions.assertArrayEquals(expectedCksum, lead.getChecksum())
    );
  }

  private void testPreface(final ZChunkHeaderPreface preface) {
    final byte[] expectedDataCksum = ByteUtils.hexStringToByteArray("772aa76adc41e290dadff603b761ba02faad4681df05030539ae0ecd925ccd05");

    Assertions.assertAll(
        () -> Assertions.assertArrayEquals(expectedDataCksum, preface.getTotalDataChecksum()),
        () -> Assertions.assertTrue(preface.getPrefaceFlags().isEmpty()),
        () -> Assertions.assertEquals(2L, preface.getCompressionAlgorithm().getCompressionTypeValue().getLongValue()),
        () -> Assertions.assertTrue(preface.getOptionalElements().isEmpty())
    );
  }

  private void testIndex(final ZChunkHeaderIndex index) {
    Assertions.assertAll(
        () -> Assertions.assertEquals(357L, index.getIndexSize().getLongValue()),
        () -> Assertions.assertEquals(IndexChecksumType.SHA512_128, index.getChunkChecksumType()),
        () -> Assertions.assertEquals(3L, index.getChunkChecksumType().getIdentifier()),
        () -> Assertions.assertEquals(17L, index.getChunkCount().getLongValue())
    );
  }

}
