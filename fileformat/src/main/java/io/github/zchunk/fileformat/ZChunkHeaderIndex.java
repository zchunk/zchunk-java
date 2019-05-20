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
import io.github.zchunk.fileformat.util.ByteUtils;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.immutables.value.Value;

/**
 * Header index.
 *
 * <pre>
 * +=================+==========================+==================+
 * | Index size (ci) | Chunk checksum type (ci) | Chunk count (ci) |
 * +=================+==========================+==================+
 *
 * (Dict stream will only exist if flag 0 is set to 1)
 * +==================+===============+==================+
 * | Dict stream (ci) | Dict checksum | Dict length (ci) |
 * +==================+===============+==================+
 *
 * +===============================+
 * | Uncompressed dict length (ci) |
 * +===============================+
 *
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
public abstract class ZChunkHeaderIndex {

  public abstract CompressedInt getIndexSize();

  public abstract CompressedInt getChunkChecksumTypeInt();

  @Value.Derived
  public IndexChecksumType getChunkChecksumType() {
    return IndexChecksumType.find(getChunkChecksumTypeInt().getValue());
  }

  public abstract CompressedInt getChunkCount();

  public abstract Optional<byte[]> getDictStream();

  /**
   * Must be all zeros if no dict present.
   *
   * @return dict checksum or all zeros.
   */
  public abstract byte[] getDictChecksum();

  /**
   * Dict length or zero.
   *
   * @return dict length.
   */
  public abstract CompressedInt getDictLength();

  public abstract CompressedInt getUncompressedDictLength();

  public abstract Map<byte[], ZChunkHeaderChunkInfo> getChunkInfo();

  @Value.Lazy
  public Set<ZChunkHeaderChunkInfo> getChunkInfoSortedByIndex() {
    final Supplier<Set<ZChunkHeaderChunkInfo>> IndexSortedList = () -> new TreeSet<>(ZChunkHeaderChunkInfo.INDEX_COMPARATOR);

    return getChunkInfo().values()
        .stream()
        .sorted(ZChunkHeaderChunkInfo.INDEX_COMPARATOR)
        .collect(Collectors.toCollection(IndexSortedList));
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ZChunkHeaderIndex.class.getSimpleName() + "[", "]")
        .add("indexSize=" + getIndexSize())
        .add("chunkChecksumType=" + getChunkChecksumType())
        .add("chunkCount=" + getChunkCount())
        .add("dictStream=" + ByteUtils.byteArrayToHexString(getDictStream().orElse(new byte[0])))
        .add("dictChecksum=" + ByteUtils.byteArrayToHexString(getDictChecksum()))
        .add("dictLength=" + getDictLength())
        .add("dictUncompressedLength=" + getUncompressedDictLength())
        .add("chunkInfo=" + getChunkInfo())
        .toString();
  }
}
