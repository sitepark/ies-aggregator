package com.sitepark.ies.aggregator.value.media;

/**
 * The empty {@link Media} asset: a placeholder used where a media field references no asset.
 *
 * <p>All accessors return neutral values ({@code 0}, {@code ""}, an empty {@link Hash} and {@link
 * Metadata} without any descriptive data), and {@link #isEmpty()} returns {@code true}. There is
 * exactly one instance, obtained via {@link Media#empty()}.
 */
public final class EmptyMedia implements Media {

  static final EmptyMedia INSTANCE = new EmptyMedia();

  private static final Hash EMPTY_HASH = new Hash(HashAlgorithm.SHA_256, "");

  private static final Metadata EMPTY_METADATA = new GenericMetadata(null, null, null, null, null);

  private EmptyMedia() {}

  @Override
  public int objectId() {
    return 0;
  }

  @Override
  public int id() {
    return 0;
  }

  @Override
  public String filename() {
    return "";
  }

  @Override
  public String originFilename() {
    return "";
  }

  @Override
  public String mimeType() {
    return "";
  }

  @Override
  public long fileSize() {
    return 0L;
  }

  @Override
  public Hash hash() {
    return EMPTY_HASH;
  }

  @Override
  public Metadata metadata() {
    return EMPTY_METADATA;
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public String toString() {
    return "EmptyMedia";
  }
}
