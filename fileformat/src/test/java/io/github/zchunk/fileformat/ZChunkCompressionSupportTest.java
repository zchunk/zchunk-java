package io.github.zchunk.fileformat;

import io.github.zchunk.compression.algo.none.NoneCompressionAlgorithm;
import io.github.zchunk.compression.algo.unknown.UnknownAlgorithm;
import io.github.zchunk.compression.algo.zstd.ZStdCompressionAlgorithm;
import io.github.zchunk.compression.api.CompressionAlgorithm;
import io.github.zchunk.compression.api.CompressionAlgorithmFactory;
import io.github.zchunk.compression.api.err.DecompressionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ZChunkCompressionSupportTest {

  @Test
  public void testCompressionSupport_unknown() {
    final CompressionAlgorithm compressionAlgorithm = CompressionAlgorithmFactory.forType(4);
    Assertions.assertAll(
        () -> Assertions.assertEquals(UnknownAlgorithm.class, compressionAlgorithm.getClass()),
        () -> Assertions.assertEquals("unknown", compressionAlgorithm.getName()),
        () -> Assertions.assertThrows(DecompressionException.class, compressionAlgorithm::getOutputStreamSupplier)
    );
  }

  @Test
  public void testCompressionSupport_none() {
    final CompressionAlgorithm compressionAlgorithm = CompressionAlgorithmFactory.forType(0L);
    Assertions.assertAll(
        () -> Assertions.assertEquals(NoneCompressionAlgorithm.class, compressionAlgorithm.getClass()),
        () -> Assertions.assertEquals("none", compressionAlgorithm.getName())
    );
  }

  @Test
  public void testCompressionSupport_zstd() {
    final CompressionAlgorithm compressionAlgorithm = CompressionAlgorithmFactory.forType(2L);
    Assertions.assertAll(
        () -> Assertions.assertEquals(ZStdCompressionAlgorithm.class, compressionAlgorithm.getClass()),
        () -> Assertions.assertEquals("zstd", compressionAlgorithm.getName())
    );
  }
}
