package com.sitepark.ies.aggregator.value.media.scaling;

import com.sitepark.ies.aggregator.value.media.Image;
import java.util.List;

/**
 * All targets to scale for a single source image, passed to an {@link
 * com.sitepark.ies.aggregator.port.ImageScaler} as one batch.
 *
 * <p>The focal point applies to the whole source image and only affects cropping; it is taken from
 * the image's own metadata ({@link com.sitepark.ies.aggregator.value.media.ImageMetadata#focalPoint()}).
 * Each output target carries its own target size and background color.
 *
 * <p>The requested {@code formats} apply to every target of this request, because the engine writes
 * all formats of a target from a single scale operation. Which of them can actually be produced is
 * reported back per target in {@link ScaledImage#encodings()}.
 *
 * @param image the image to scale
 * @param formats the output formats to produce, or an empty list to let the adapter derive its
 *     default from the source image's own format
 * @param targets the output targets to produce
 */
public record ScaleImageRequest(Image image, List<Format> formats, List<ScaleImageTarget> targets) {

  public ScaleImageRequest {
    formats = List.copyOf(formats);
    targets = List.copyOf(targets);
  }

  /** Creates a scale request. */
  public static ScaleImageRequest of(
      Image image, List<Format> formats, List<ScaleImageTarget> targets) {
    return new ScaleImageRequest(image, formats, targets);
  }

  /** Creates a scale request without an explicit output format. */
  public static ScaleImageRequest of(Image image, List<ScaleImageTarget> targets) {
    return new ScaleImageRequest(image, List.of(), targets);
  }
}
