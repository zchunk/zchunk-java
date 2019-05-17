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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ZChunkHeaderFactoryTest {

  @Test
  public void testKnownPrefaceFlags() throws IOException {
    final ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{0b00000001});
    final CompressedInt ci = ZChunkHeaderFactory.getPrefaceFlagsFromInputStream(in);
    final Set<PrefaceFlag> prefaceFlags = PrefaceFlag.getPrefaceFlags(ci);

    Assertions.assertAll(
        () -> Assertions.assertEquals(1, prefaceFlags.size()),
        () -> Assertions.assertEquals(PrefaceFlag.HAS_DATA_STREAMS, prefaceFlags.iterator().next())
    );
  }


}
