/*
 *  Copyright 2018 The zchunk-java contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.zchunk.fileformat.io;

import java.io.IOException;
import java.io.InputStream;

public class BoundedInputStream extends InputStream {

  private final InputStream in;
  private final long limit;
  private long readCount = 0L;

  public BoundedInputStream(final InputStream in, final long limit) {
    this.in = in;
    this.limit = limit;
  }

  @Override
  public int read() throws IOException {
    if (this.readCount >= this.limit) {
      return -1;
    }

    this.readCount++;
    return this.in.read();
  }
}
