package com.sitepark.ies.aggregator.value.media.scaling;

/**
 * A concrete size the scaling engine should produce for a {@link RequestedSize}.
 *
 * <p>The dimensions are kept as {@code double} to stay faithful to the original computation; rounding
 * to whole pixels is left to the scaling engine that consumes this value.
 *
 * @param width the width to scale to, in pixels
 * @param height the height to scale to, in pixels
 * @param padding the effective padding mode to apply
 * @param idealWidth the requested width before any downscaling to fit the source, in pixels
 * @param idealHeight the requested height before any downscaling to fit the source, in pixels
 */
public record ComputedSize(
    double width, double height, Padding padding, double idealWidth, double idealHeight) {

  /** Creates a computed size. */
  public static ComputedSize of(
      double width, double height, Padding padding, double idealWidth, double idealHeight) {
    return new ComputedSize(width, height, padding, idealWidth, idealHeight);
  }
}
