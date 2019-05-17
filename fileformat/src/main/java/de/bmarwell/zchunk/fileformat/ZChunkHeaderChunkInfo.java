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
import de.bmarwell.zchunk.fileformat.util.ByteUtils;
import java.util.Optional;
import java.util.StringJoiner;
import org.immutables.value.Value;

/**
 * <pre>
 * (Chunk stream will only exist if flag 0 is set to 1)
 * [+===================+================+===================+
 * [| Chunk stream (ci) | Chunk checksum | Chunk length (ci) |
 * [+===================+================+===================+
 *
 * +==========================+]
 * | Uncompressed length (ci) |] ...
 * +==========================+]
 * </pre>
 */

@Value.Immutable
public abstract class ZChunkHeaderChunkInfo {

  public abstract long getCurrentIndex();

  public abstract Optional<byte[]> getChunkStream();

  public abstract byte[] getChunkChecksum();

  public abstract CompressedInt getChunkLength();

  public abstract CompressedInt getChunkUncompressedLength();

  @Override
  public String toString() {
    return new StringJoiner(", ", ZChunkHeaderChunkInfo.class.getSimpleName() + "[", "]")
        .add("index=" + getCurrentIndex())
        .add("chunkStream=" + ByteUtils.byteArrayToHexString(getChunkStream().orElse(new byte[0])))
        .add("chunkChecksum=" + ByteUtils.byteArrayToHexString(getChunkChecksum()))
        .add("chunkLength=" + getChunkLength())
        .add("chunkUncompressedLength=" + getChunkUncompressedLength())
        .toString();
  }
}
