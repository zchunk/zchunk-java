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

import io.github.zchunk.fileformat.util.ChecksumUtil;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UnzckTest {

  private static final Logger LOG = Logger.getLogger(UnzckTest.class.getCanonicalName());

  @Test
  public void testUnzckDict() throws NoSuchAlgorithmException, IOException {
    final ClassLoader classLoader = getClass().getClassLoader();
    final String pathToFiles = classLoader.getResource("files").getFile();

    final File input = new File(pathToFiles, "LICENSE.dict.fodt.zck");
    Assertions.assertTrue(input.exists());
    Assertions.assertTrue(input.isFile());
    Assertions.assertTrue(input.canRead());

    LOG.finer("File found: " + input.getAbsolutePath());
    final File targetFile = new File(pathToFiles, "LICENSE.dict.fodt.zdict");

    final Unzck unzck = new Unzck();
    unzck.setInputFile(input);
    unzck.setOutputFile(targetFile);
    unzck.setDictOnly(true);
    unzck.call();

    final MessageDigest md5 = MessageDigest.getInstance("md5");
    final byte[] bytes = ChecksumUtil.calculateFileChecksum(targetFile, md5);
    final String foundMd5 = new BigInteger(1, bytes).toString(16);

    Assertions.assertEquals("e051cbdf211c13bead2009e49b3317f5", foundMd5);
  }

}
