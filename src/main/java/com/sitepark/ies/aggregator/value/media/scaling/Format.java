package com.sitepark.ies.aggregator.value.media.scaling;

import com.sitepark.ies.aggregator.value.NamedEnum;

/**
 * An output image format an {@link com.sitepark.ies.aggregator.port.ImageScaler} can produce.
 *
 * <p>The external name (see {@link #getName()}) is the token used in configuration (e.g. {@code
 * "avif"}); {@link #mimeType()} is the IANA media type an HTML {@code <source type="…">} needs.
 */
public enum Format implements NamedEnum {

  /** AV1 Image File Format. */
  AVIF("avif", "image/avif"),

  /** WebP. */
  WEBP("webp", "image/webp"),

  /** JPEG. */
  JPEG("jpeg", "image/jpeg"),

  /** PNG. */
  PNG("png", "image/png"),

  /** GIF. */
  GIF("gif", "image/gif");

  private final String name;

  private final String mimeType;

  Format(String name, String mimeType) {
    this.name = name;
    this.mimeType = mimeType;
  }

  @Override
  public String getName() {
    return this.name;
  }

  /**
   * Returns the media type this format is served as.
   *
   * @return the IANA media type, e.g. {@code "image/avif"}
   */
  public String mimeType() {
    return this.mimeType;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
