package com.sitepark.ies.aggregator.value.media.scaling;

/**
 * The unscaled source image as seen by an image size calculation.
 *
 * <p>This is a calculation-only view carrying just the pixel dimensions and whether the image is a
 * scalable vector. A scalable vector (e.g. SVG) can be enlarged without loss, so the
 * no-enlargement / no-distortion cropping rules are skipped for it.
 *
 * @param width the original width in pixels
 * @param height the original height in pixels
 * @param scalableVector whether the image is a scalable vector graphic (e.g. SVG)
 */
public record SourceImage(int width, int height, boolean scalableVector) {

  /** Creates a source image. */
  public static SourceImage of(int width, int height, boolean scalableVector) {
    return new SourceImage(width, height, scalableVector);
  }
}
