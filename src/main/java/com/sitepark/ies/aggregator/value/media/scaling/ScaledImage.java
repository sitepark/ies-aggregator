package com.sitepark.ies.aggregator.value.media.scaling;

import com.sitepark.ies.aggregator.value.uri.Uri;

/**
 * A single scaled image produced by an {@link com.sitepark.ies.aggregator.port.ImageScaler}.
 *
 * <p>Carries exactly the fields a consumer needs from a scaled rendition: its URL and the actual
 * result dimensions in pixels. The URL is provided by the adapter in the form appropriate for the
 * current output mode (a published file URL when publishing, a binary-servlet URL when editing).
 *
 * @param url the URL under which the scaled image is available
 * @param width the actual result width in pixels
 * @param height the actual result height in pixels
 */
public record ScaledImage(Uri url, int width, int height) {

  /** Creates a scaled image. */
  public static ScaledImage of(Uri url, int width, int height) {
    return new ScaledImage(url, width, height);
  }
}
