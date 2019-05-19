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

import io.github.zchunk.compressedint.CompressedInt;
import io.github.zchunk.compression.api.CompressionAlgorithm;
import io.github.zchunk.fileformat.util.ByteUtils;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import org.immutables.value.Value;

/**
 * <p><code>
 * +===============+============+========================+<br>
 * | Data checksum | Flags (ci) | Compression type (ci ) |<br>
 * +===============+============+========================+<br>
 * <br>
 * (Optional elements will only be set if flag 1 is set to 1)<br>
 * +=============================+<br>
 * | Optional element count (ci) |<br>
 * +=============================+<br>
 * <br>
 * [+==========================+=================================+<br>
 * [| Optional element id (ci) | Optional element data size (ci) |<br>
 * [+==========================+=================================+<br>
 * <br>
 * +=======================+]<br>
 * | Optional element data |] ...<br>
 * +=======================+]<br>
 * </code><br></p>
 */
@Value.Immutable
public abstract class ZChunkHeaderPreface {

  /**
   * Returns the checksum of the data segment.
   *
   * @return the checksum of the data segment.
   */
  public abstract byte[] getTotalDataChecksum();

  public abstract CompressedInt getPrefaceFlagsInt();

  public abstract Set<PrefaceFlag> getPrefaceFlags();

  @Value.Derived
  public boolean hasOptionalElements() {
    return getPrefaceFlags().contains(PrefaceFlag.HAS_OPTIONAL_ELEMENTS);
  }

  public abstract CompressionAlgorithm getCompressionAlgorithm();

  public abstract CompressedInt getOptionalElementCount();

  public abstract List<OptionalElement> getOptionalElements();

  /**
   * Flags
   * This is a compressed integer containing a bitmask of the flags.  All unused
   * flags MUST be set to 0.  If a decoder sees a flag set that it doesn't
   * recognize, it MUST exit with an error.
   */
  @Value.Check
  public void testUnknownFlags() {
    final Set<PrefaceFlag> flags = getPrefaceFlags();

    if (flags.contains(PrefaceFlag.HAS_DATA_STREAMS)) {
      throw new UnsupportedOperationException("Not implemented: " + PrefaceFlag.HAS_DATA_STREAMS);
    }

    if (flags.contains(PrefaceFlag.HAS_OPTIONAL_ELEMENTS)) {
      throw new UnsupportedOperationException("Not implemented: " + PrefaceFlag.HAS_OPTIONAL_ELEMENTS);
    }
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ZChunkHeaderPreface.class.getSimpleName() + "[", "]")
        .add("cksum='" + ByteUtils.byteArrayToHexString(getTotalDataChecksum()) + "'")
        .add("flags='" + getPrefaceFlags() + "'")
        .add("compressionAlgorithm=" + getCompressionAlgorithm())
        .add("optionalElementCount=" + getOptionalElementCount())
        .add("optionalElements=" + getOptionalElements())
        .toString();
  }


}
