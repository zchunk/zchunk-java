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

import de.bmarwell.zchunk.compressedint.CompressedInt;
import de.bmarwell.zchunk.fileformat.err.InvalidFileException;
import de.bmarwell.zchunk.fileformat.util.ByteUtils;
import java.util.Arrays;
import java.util.StringJoiner;
import org.immutables.value.Value;

/**
 * The lead:<br>
 *
 * <p><code>
 * +-+-+-+-+-+====================+==================+=================+<br>
 * |   ID    | Checksum type (ci) | Header size (ci) | Header checksum |<br>
 * +-+-+-+-+-+====================+==================+=================+
 * </code></p>
 */
@Value.Immutable
public abstract class ZChunkHeaderLead {

  public abstract byte[] getId();

  public abstract CompressedInt getChecksumTypeInt();

  @Value.Derived
  public HeaderChecksumType getChecksumType() {
    return HeaderChecksumType.values()[getChecksumTypeInt().getIntValue()];
  }

  /**
   * Header size:
   * This is an integer containing the size of the header, not including the lead.
   *
   * @return Remaining bytes after the header.
   */
  public abstract CompressedInt getHeaderSize();

  /**
   * Checksum of the whole header.
   *
   * @return the checksum of the whole header.
   */
  public abstract byte[] getChecksum();

  @Value.Check
  public void checkLead() {
    if (!Arrays.equals(ZChunkConstants.Header.FILE_MAGIC, this.getId())) {
      throw new InvalidFileException("file magic differs: [" + ByteUtils.byteArrayToHexString(this.getId()) + "].");
    }
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ZChunkHeaderLead.class.getSimpleName() + "[", "]")
        .add("id='" + ByteUtils.byteArrayToHexString(getId()) + "'")
        .add("cksumtype=" + getChecksumTypeInt().getValue().toString())
        .add("cksumtype='" + getChecksumType() + "'")
        .add("headerSize=" + getHeaderSize().getValue().toString())
        .add("cksum='" + ByteUtils.byteArrayToHexString(getChecksum()) + "'")
        .toString();
  }
}
