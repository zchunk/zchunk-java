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
import io.github.zchunk.fileformat.ZChunkHeader;
import io.github.zchunk.fileformat.ZChunkHeaderChunkInfo;
import io.github.zchunk.fileformat.ZChunkHeaderIndex;
import io.github.zchunk.fileformat.util.IOUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(description = "Unpacks the completely downloaded zck file.",
         name = "unzck",
         mixinStandardHelpOptions = true)
public class Unzck implements Callable<Integer> {

  private static final Logger LOG = Logger.getLogger(Unzck.class.getCanonicalName());

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

    return decompressFile(zChunkFile);
  }

  private int decompressFile(final ZChunkFile zChunkFile) {
    final File target = getTargetFile();

    try (final FileOutputStream fileOutputStream = new FileOutputStream(target)) {
      final File targetDir = target.getAbsoluteFile().getParentFile();
      if (null == targetDir) {
        throw new IllegalStateException("TargetDir Parent is null: [" + target.getAbsolutePath() + "].");
      }
      targetDir.mkdirs();
      target.createNewFile();

      final ZChunkHeader zChunkFileHeader = zChunkFile.getHeader();
      final ZChunkHeaderIndex zChunkHeaderIndex = zChunkFileHeader.getIndex();
      if (zChunkHeaderIndex.getDictLength().getIntValue() == 0) {
        throw new UnsupportedOperationException("TODO: uncompress without dict");
      }

      final byte[] decompressedDict = ZChunk.getDecompressedDict(zChunkFileHeader, this.inputFile);

      uncompressChunks(fileOutputStream, zChunkFileHeader, decompressedDict);

    } catch (final FileNotFoundException fnfe) {
      cleanPartialFile(target);
      throw new UncompressException("Unable to create parent dir or file: [" + target.getAbsolutePath() + "].", fnfe);
    } catch (final IOException ex) {
      cleanPartialFile(target);
      throw new UncompressException("Unable to write file: [" + target.getAbsolutePath() + "].", ex);
    }

    return 0;
  }

  private void uncompressChunks(final FileOutputStream fileOutputStream,
                                final ZChunkHeader zChunkFileHeader,
                                final byte[] decompressedDict) throws IOException {
    final SortedSet<ZChunkHeaderChunkInfo> chunks = zChunkFileHeader.getIndex().getChunkInfoSortedByIndex();

    // TODO: This can be optimized using random access file and parallel writing.
    for (final ZChunkHeaderChunkInfo chunk : chunks) {
      LOG.finest("Working on chunk [" + chunk + "].");
      final InputStream decompressedChunk = ZChunk.getDecompressedChunk(
          zChunkFileHeader,
          this.inputFile,
          decompressedDict,
          chunk.getCurrentIndex());
      IOUtil.copy(decompressedChunk, fileOutputStream);
    }
  }

  private int decompressDict(final ZChunkFile zChunkFile) {
    final File target = getTargetFile();
    try (final FileOutputStream fileOutputStream = new FileOutputStream(target)) {
      final File targetDir = target.getAbsoluteFile().getParentFile();
      if (null == targetDir) {
        throw new IllegalStateException("TargetDir Parent is null: [" + target.getAbsolutePath() + "].");
      }
      targetDir.mkdirs();
      target.createNewFile();
      final InputStream decompressedDictStream = ZChunk.getDecompressedDictStream(zChunkFile.getHeader(), this.inputFile);
      final int copied = IOUtil.copy(decompressedDictStream, fileOutputStream);

    } catch (final FileNotFoundException fnfe) {
      cleanPartialFile(target);
      throw new UncompressException("Unable to create parent dir or file: [" + target.getAbsolutePath() + "].", fnfe);
    } catch (final IOException ex) {
      cleanPartialFile(target);
      throw new UncompressException("Unable to write file: [" + target.getAbsolutePath() + "].", ex);
    }

    return 0;
  }

  private void cleanPartialFile(final File target) {
    if (target.exists()) {
      try {
        Files.delete(target.toPath());
      } catch (final IOException ioEx) {
        LOG.log(Level.WARNING, ioEx, () -> "unable to delete file [" + target.getAbsolutePath() + "].");
      }
    }
  }

  private File getTargetFile() {
    if (null != this.outputFile) {
      return this.outputFile;
    }

    if (this.dictOnly) {
      return ZChunkFilename.getDictFile(this.inputFile);
    }

    return ZChunkFilename.getNormalFile(this.inputFile);
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
