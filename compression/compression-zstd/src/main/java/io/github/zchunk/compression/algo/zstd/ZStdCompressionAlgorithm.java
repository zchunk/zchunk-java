package io.github.zchunk.compression.algo.zstd;

import com.github.luben.zstd.ZstdInputStream;
import io.github.zchunk.compressedint.CompressedInt;
import io.github.zchunk.compressedint.CompressedIntFactory;
import io.github.zchunk.compression.api.CompressionAlgorithm;
import io.github.zchunk.compression.api.err.DecompressionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.BiFunction;

public class ZStdCompressionAlgorithm implements CompressionAlgorithm {

  private static final CompressedInt TWO = CompressedIntFactory.valueOf(2L);
  private static final String ALGORITHM_NAME_ZSTD = "zstd";

  @Override
  public CompressedInt getCompressionTypeValue() {
    return TWO;
  }

  @Override
  public String getName() {
    return ALGORITHM_NAME_ZSTD;
  }

  @Override
  public BiFunction<InputStream, byte[], InputStream> getOutputStreamSupplier() {
    return createZstdInputStream();
  }

  private BiFunction<InputStream, byte[], InputStream> createZstdInputStream() {
    return (compressedInputStream, dict) -> {
      try {
        final ZstdInputStream zstdInputStream = new ZstdInputStream(compressedInputStream);
        if (!Arrays.equals(new byte[0], dict)) {
          zstdInputStream.setDict(dict);
        }

        return zstdInputStream;
      } catch (final IOException e) {
        throw new DecompressionException("Unable to create input stream.");
      }
    };
  }


}
