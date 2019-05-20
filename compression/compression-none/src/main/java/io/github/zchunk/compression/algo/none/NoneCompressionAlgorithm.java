package io.github.zchunk.compression.algo.none;

import io.github.zchunk.compressedint.CompressedInt;
import io.github.zchunk.compressedint.CompressedIntFactory;
import io.github.zchunk.compression.api.CompressionAlgorithm;

import java.io.InputStream;
import java.util.function.BiFunction;

public class NoneCompressionAlgorithm implements CompressionAlgorithm {

  @Override
  public CompressedInt getCompressionTypeValue() {
    return CompressedIntFactory.valueOf(0L);
  }

  @Override
  public String getName() {
    return "none";
  }

  @Override
  public BiFunction<InputStream, byte[], InputStream> getOutputStreamSupplier() {
    return (a, b) -> a;
  }
}
