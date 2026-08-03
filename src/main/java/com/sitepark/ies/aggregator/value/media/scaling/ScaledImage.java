package com.sitepark.ies.aggregator.value.media.scaling;

import java.util.List;

/**
 * A single scaled image produced by an {@link com.sitepark.ies.aggregator.port.ImageScaler}.
 *
 * <p>Carries the geometry once and one {@link Encoded} per output format that was actually written.
 * The engine produces all encodings of a target from a single scale operation, so they share these
 * dimensions and differ only in format and URL. The URLs are provided by the adapter in the form
 * appropriate for the current output mode (a published file URL when publishing, a binary-servlet URL
 * when editing).
 *
 * @param width the actual result width in pixels
 * @param height the actual result height in pixels
 * @param encodings the encodings produced for this size, in the order the adapter wrote them; the
 *     first one is the primary encoding
 */
public record ScaledImage(int width, int height, List<Encoded> encodings) {

  public ScaledImage {
    encodings = List.copyOf(encodings);
  }

  /** Creates a scaled image. */
  public static ScaledImage of(int width, int height, List<Encoded> encodings) {
    return new ScaledImage(width, height, encodings);
  }
}
