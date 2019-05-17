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

package de.bmarwell.zchunk.compressedint;

import java.math.BigInteger;
import org.immutables.value.Value;

public interface CompressedInt {

  byte[] getCompressedBytes();

  /**
   * If you have few experience with unsigned values in java, consider using this value instead.
   *
   * @return a biginteger which will always output a positive value.
   */
  @Value.Lazy
  BigInteger getValue();

  @Value.Lazy
  long getLongValue();

  @Value.Lazy
  long getUnsignedLongValue();

  @Value.Lazy
  int getIntValue();
}
