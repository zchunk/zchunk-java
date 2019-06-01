# zchunk-java

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0
)   [![Build Status](https://travis-ci.org/zchunk/zchunk-java.svg?branch=master)](https://travis-ci.org/zchunk/zchunk-java
)   [![codecov](https://codecov.io/gh/zchunk/zchunk-java/branch/master/graph/badge.svg)](https://codecov.io/gh/zchunk/zchunk-java
)


This is a **pure java-only implementation** of the [ZChunk](https://github.com/zchunk/zchunk) file format.
It does not hard depend on other libraries and can be used without any external or transitive dependencies.

## What is zchunk?

**zchunk** is a compressed file format that splits the file into independent chunks.
This allows you to only download changed chunks when downloading a new version
of the file, and also makes zchunk files efficient over rsync.

zchunk files are protected with strong checksums to verify that the file you
downloaded is, in fact, the file you wanted.

## What is the goal of this project?

The goal is to create a pure java implementation of the zchunk file format.
This way, java programs or Android apps will be able to use the zchunk file format. 


## Which dependencies do I need?

**zchunk-java** can be used *without any* transitives dependencies/libraries.

However, to have support for compression, you should use the arteifact `zchunk-bundle-lib` (TBD), which also
pulls in support for `zstd` compression and maybe other compressions later.

The command line application (`zchunk-app`) is another matter. It uses `picocli` for parsing command lines,
but other than that, it does not have any new dependencies. Since `picocli` is bundled with the app in an
executable `one-jar`, there is no manual copying of dependencies.


## Runtime Requirements and usage

  * Java 8 or newer, any JDK (adopt and adopt-openj9 work well).
  

### To read a file header:

```java
class Main {
  static void main(String[] args) {
    ZChunkFile zck = ZChunk.fromFile(inputFile);
    boolean isValid = ChecksumUtil.isValid(zck.getHeader());   
  }
}
```

### writing a file

=> TBD

### verifying checksum data

```java
class Main {
  static void main(String[] args) {
    ZChunkFile zck = ZChunk.fromFile(inputFile);
    // total file validation (header, all single chunks, all chunk data).
    boolean isValid = ZChunk.validateFile(inputFile);
    
    
    // partial checksums:
    boolean isValidHeader = ChecksumUtil.isValidHeader(zck.getHeader(), inputFile);
    boolean isAllChunksValid = ChecksumUtil.allChunksAreValid(zck.getHeader(), inputFile);
    boolean isValidData = ChecksumUtil.isValidData(zck.getHeader(), inputFile);
  }
}
```

### Downloading chunks

  * locally
  * Over HTTPS and HTTP/2.0
  * Over ssh/scp
    * This is not even implemented in the original code.
    * This can be done by executing `dd if=file bs=1 count=X` or similar.
  
=> TBD

### executable jars

=> TBD
  
## Building from source
 
### Build requirements
  
  * Maven 3.3.1 or newer, or just use `mvnw`.

### Installation

  * `./mvnw clean install`
  



