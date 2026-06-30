package com.sitepark.ies.aggregator.value.media;

/** Relative focal point of an image; both axes in the range [0.0, 1.0]. */
public record FocalPoint(float x, float y) {

  /** The center focal point (0.5, 0.5). */
  public static final FocalPoint CENTER = new FocalPoint(0.5f, 0.5f);

  public FocalPoint {
    if (x < 0.0f || x > 1.0f) {
      throw new IllegalArgumentException("x must be in [0.0, 1.0], but was " + x);
    }
    if (y < 0.0f || y > 1.0f) {
      throw new IllegalArgumentException("y must be in [0.0, 1.0], but was " + y);
    }
  }
}
