package com.sitepark.ies.aggregator.value.media.scaling;

import com.sitepark.ies.aggregator.value.media.Image;
import com.sitepark.ies.aggregator.value.uri.UriTarget;
import java.util.List;

/**
 * All targets to scale for a single source image, passed to an {@link
 * com.sitepark.ies.aggregator.port.ImageScaler} as one batch.
 *
 * <p>The {@code focalPoint} applies to the whole source image and only affects cropping (see {@link
 * com.sitepark.ies.aggregator.port.ImageScaler}); each output target carries its own target size
 * and background color.
 *
 * @param image the image to scale (e.g. {@link UriTarget#ofMedia(int, int)})
 * @param targets the output targets to produce
 */
public record ScaleImageRequest(Image image, List<ScaleImageTarget> targets) {

  public ScaleImageRequest {
    targets = List.copyOf(targets);
  }

  /** Creates a scale request. */
  public static ScaleImageRequest of(Image image, List<ScaleImageTarget> targets) {
    return new ScaleImageRequest(image, targets);
  }
}
