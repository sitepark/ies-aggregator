package com.sitepark.ies.aggregator.value.media.scaling;

/**
 * A concrete size the scaling engine should produce.
 *
 * <p>The dimensions are kept as {@code double} to stay faithful to the original computation; rounding
 * to whole pixels is left to the scaling engine that consumes this value.
 *
 * @param width the width to scale to, in pixels
 * @param height the height to scale to, in pixels
 * @param fitMode how the image is fitted into that size
 */
public record ComputedSize(double width, double height, FitMode fitMode) {

  /** Creates a computed size. */
  public static ComputedSize of(double width, double height, FitMode fitMode) {
    return new ComputedSize(width, height, fitMode);
  }
}
