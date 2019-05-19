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

package io.github.zchunk.fileformat.err;

import java.io.File;
import java.util.Optional;
import java.util.StringJoiner;
import org.checkerframework.checker.nullness.qual.Nullable;

public class InvalidFileException extends RuntimeException {

  private static final long serialVersionUID = -1329481614175727163L;

  private final @Nullable File sourceFile;

  public InvalidFileException(final String message) {
    super(message);
    this.sourceFile = null;
  }

  public InvalidFileException(final String message, final File zckFile) {
    super(message);
    this.sourceFile = zckFile;
  }

  public InvalidFileException(final String message, final File zckFile, final Throwable cause) {
    super(message, cause);
    this.sourceFile = zckFile;
  }

  public Optional<File> getSourceFile() {
    return Optional.ofNullable(this.sourceFile);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", InvalidFileException.class.getSimpleName() + "[", "]")
        .add("super=" + super.toString())
        .add("sourceFile='" + getSourceFile().map(File::getAbsolutePath).orElse("unknown") + "'")
        .toString();
  }
}
