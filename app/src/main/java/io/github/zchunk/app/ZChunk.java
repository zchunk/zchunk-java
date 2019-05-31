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

package io.github.zchunk.app;

import io.github.zchunk.app.commands.Unzck;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.UnmatchedArgumentException;

@Command(
    name = "ZChunk",
    subcommands = {
        Unzck.class
    },
    version = "1.0"
)
public class ZChunk implements Callable<Integer> {

  @Option(names = {"-v", "--verbose"}, description = "Be verbose.")
  private boolean verbose;

  @Option(names = {"-h", "-?", "--help", "--usage"}, usageHelp = true, description = "display a help message")
  private boolean helpRequested;

  @Option(names = {"-V", "--version"}, versionHelp = true)
  private boolean showVersion;



  /*
    Usage: unzck [OPTION...] <file>
    unzck - Decompress a zchunk file

      -c, --stdout               Direct output to stdout
          --dict                 Only extract the dictionary
      -v, --verbose              Increase verbosity (can be specified more than
                                 once for debugging)
      -?, --help                 Give this help list
          --usage                Give a short usage message
      -V, --version              Show program version
   */

  public static int main(final String[] args) {
    final CommandLine cmd = new CommandLine(new ZChunk());

    try {
      final ParseResult parseResult = cmd.parseArgs(args);

      if (cmd.isUsageHelpRequested()) {
        cmd.usage(cmd.getOut());
        return cmd.getCommandSpec().exitCodeOnUsageHelp();
      }

      if (cmd.isVersionHelpRequested()) {
        cmd.printVersionHelp(cmd.getOut());
        return cmd.getCommandSpec().exitCodeOnVersionHelp();
      }

    } catch (final ParameterException ex) {
      cmd.getErr().println(ex.getMessage());
      if (!UnmatchedArgumentException.printSuggestions(ex, cmd.getErr())) {
        ex.getCommandLine().usage(cmd.getErr());
      }
      return cmd.getCommandSpec().exitCodeOnInvalidInput();
    }

    try {
      return cmd.execute(args);
    } catch (final RuntimeException ex) {
      // exception occurred in business logic
      ex.printStackTrace(cmd.getErr());
      return cmd.getCommandSpec().exitCodeOnExecutionException();
    }

  }

  @Override
  public Integer call() throws Exception {

    return -1;
  }
}
