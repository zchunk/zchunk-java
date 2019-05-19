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

package io.github.zchunk.fileformat.util;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ByteUtilTest {

  @Test
  public void testDecrease() {
    final BigInteger testLong = new BigInteger(1, new byte[]{0b00001000});

    final AtomicReference<BigInteger> reference = new AtomicReference<>(testLong);
    final boolean decrease = ByteUtils.decrease(reference, (long) 0b00001000);

    Assertions.assertAll(
        () -> Assertions.assertTrue(decrease),
        () -> Assertions.assertEquals(0L, reference.get().longValueExact())
    );
  }

  @Test
  public void testDecrease_other() {
    final BigInteger testLong = new BigInteger(1, new byte[]{0b00001000});

    final AtomicReference<BigInteger> reference = new AtomicReference<>(testLong);
    final boolean decrease = ByteUtils.decrease(reference, (long) 0b00000001);

    // nothing was decreased, the testlong should still have its old value.
    Assertions.assertAll(
        () -> Assertions.assertFalse(decrease),
        () -> Assertions.assertEquals(testLong.longValueExact(), reference.get().longValueExact())
    );
  }
}
