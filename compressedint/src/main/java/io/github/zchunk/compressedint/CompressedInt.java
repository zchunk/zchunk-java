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

package io.github.zchunk.compressedint;

import java.math.BigInteger;
import org.immutables.value.Value;

public interface CompressedInt {

  /**
   * Return the compressed int as compressed bytes. This is useful to determine the original arrray length.
   *
   * @return the compressed int as bytes.
   */
  byte[] getCompressedBytes();

  /**
   * If you have few experience with unsigned values in java, consider using this value instead.
   *
   * @return a biginteger which will always output a positive value.
   */
  @Value.Lazy
  BigInteger getValue();

  /**
   * Returns the value as signed(!) long value. As the compressedInt value may exceed {@link Long#MAX_VALUE}, it might throw an {@link
   * ArithmeticException}.
   *
   * @return the value as signed long (normal java use).
   * @throws ArithmeticException
   *     if the value exceeds {@link Long#MAX_VALUE}.
   */
  @Value.Lazy
  long getLongValue();

  /**
   * Returns the value as <b>unsigned</b> long value, so that it can overflow.
   *
   * <p>If your input value was {@code 0xffffffffffffffff}, this method will throw no exception and happily overflow and return {@code
   * -1L}.</p>
   */
  @Value.Lazy
  long getUnsignedLongValue();

  /**
   * Tries to fit the value into an int. As int conversion is often needed for array indexing, this will always throw an {@link
   * ArithmeticException}, if
   * the content value does not fit into a signed integer.
   *
   * @return an int representing the {@link #getValue()} value.
   */
  @Value.Lazy
  int getIntValue();
}
