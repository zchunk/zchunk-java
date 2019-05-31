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

package io.github.zchunk.app.commands;

import io.github.zchunk.app.ZChunkFilename;
import io.github.zchunk.app.err.UncompressException;
import io.github.zchunk.fileformat.ZChunk;
import io.github.zchunk.fileformat.ZChunkFile;
import io.github.zchunk.fileformat.util.IOUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import org.checkerframework.checker.nullness.qual.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(description = "Unpacks the completely downloaded zck file.",
         name = "unzck",
         mixinStandardHelpOptions = true)
public class Unzck implements Callable<Integer> {

  @Option(names = {"-c", "--stdout"})
  private boolean toStdOut;

  @Option(names = {"--dict"})
  private boolean dictOnly;

  @Option(names = {"-o"})
  private @Nullable File outputFile;

  @Parameters(arity = "1", paramLabel = "FILE")
  @SuppressWarnings(value = {"initialization.fields.uninitialized", "dereference.of.nullable"})
  private File inputFile;

  @Override
  public Integer call() {
    final ZChunkFile zChunkFile = ZChunk.fromFile(this.inputFile);
    if (this.dictOnly) {
      return decompressDict(zChunkFile);
    }

    return -1;
  }

  private int decompressDict(final ZChunkFile zChunkFile) {
    final File target = getTargetFile();
    try {
      final File targetDir = target.getAbsoluteFile().getParentFile();
      if (null == targetDir) {
        throw new IllegalStateException("TargetDir Parent is null: [" + target.getAbsolutePath() + "].");
      }
      targetDir.mkdirs();
      target.createNewFile();
      final InputStream decompressedDictStream = ZChunk.getDecompressedDictStream(zChunkFile.getHeader(), this.inputFile);
      final FileOutputStream fileOutputStream = new FileOutputStream(target);
      final int copied = IOUtil.copy(decompressedDictStream, fileOutputStream);

    } catch (final FileNotFoundException fnfe) {
      throw new UncompressException("Unable to create parent dir or file: [" + target.getAbsolutePath() + "].", fnfe);
    } catch (final IOException ex) {
      throw new UncompressException("Unable to write file: [" + target.getAbsolutePath() + "].", ex);
    }

    return 0;
  }

  private File getTargetFile() {
    if (null != this.outputFile) {
      return this.outputFile;
    }

    return ZChunkFilename.getDictFile(this.inputFile);
  }

  public boolean isToStdOut() {
    return this.toStdOut;
  }

  public void setToStdOut(final boolean toStdOut) {
    this.toStdOut = toStdOut;
  }

  public boolean isDictOnly() {
    return this.dictOnly;
  }

  public void setDictOnly(final boolean dictOnly) {
    this.dictOnly = dictOnly;
  }

  public @Nullable File getOutputFile() {
    return this.outputFile;
  }

  public void setOutputFile(final @Nullable File outputFile) {
    this.outputFile = outputFile;
  }

  public File getInputFile() {
    return this.inputFile;
  }

  public void setInputFile(final File inputFile) {
    this.inputFile = inputFile;
  }


  @Override
  public String toString() {
    return new StringJoiner(", ", Unzck.class.getSimpleName() + "[", "]")
        .add("toStdOut=" + this.toStdOut)
        .add("dictOnly=" + this.dictOnly)
        .add("outputFile=" + this.outputFile)
        .add("inputFile=" + this.inputFile)
        .toString();
  }
}
