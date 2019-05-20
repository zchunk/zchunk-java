package io.github.zchunk.compression.api.err;

public class DecompressionException extends RuntimeException {

  private static final long serialVersionUID = 8784156714006343592L;

  public DecompressionException(String message) {
    super(message);
  }
}
