# zchunk-java

zchunk is a compressed file format that splits the file into independent chunks.
This allows you to only download changed chunks when downloading a new version
of the file, and also makes zchunk files efficient over rsync.

zchunk files are protected with strong checksums to verify that the file you
downloaded is, in fact, the file you wanted.

**As of zchunk-1.0, the ABI and API have been marked stable, and the only changes
allowed are backwards-compatible additions**

## Requirements

  * Java 8 or newer
  * Maven 

## Installation

  * `mvn clean install`
  
## Usage

### To read a file header:

```java
class Main {
  static void main(String[] args) {
    ZChunkFile zck = ZChunkHeaderFactory.fromFile(inputFile);
    boolean isValid = ChecksumUtil.isValid(zck.getHeader());   
  }
}
```

### writing a file

=> TBD

### verifying checksum data

=> TBD

### Downloading chunks

  * locally
  * Over ssh/scp
  * Over HTTPS and HTTP/2.0
    * This is not even implemented in the original code.
    * This can be done by executing `dd if=file bs=1 count=X` or similar.
  
=> TBD

### executable jars

=> TBD

