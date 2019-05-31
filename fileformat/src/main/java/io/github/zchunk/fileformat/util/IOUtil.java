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

package io.github.zchunk.fileformat.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class IOUtil {

  private static final int BUFFER_SIZE = 1024;
  private static final int EOF = -1;

  private IOUtil() {
    // util class
  }

  public static int copy(final InputStream in, final OutputStream out) throws IOException {
    final byte[] buffer = new byte[BUFFER_SIZE];
    int readCount;
    int totalWritten = 0;

    while ((readCount = in.read(buffer)) != EOF) {
      out.write(buffer, 0, readCount);
      totalWritten += readCount;

      if (readCount < BUFFER_SIZE) {
        // end reached.
        break;
      }
    }

    return totalWritten;
  }

}
