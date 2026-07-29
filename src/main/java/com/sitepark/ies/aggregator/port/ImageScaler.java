package com.sitepark.ies.aggregator.port;

import com.sitepark.ies.aggregator.value.media.scaling.ComputedSize;
import com.sitepark.ies.aggregator.value.media.scaling.Encoded;
import com.sitepark.ies.aggregator.value.media.scaling.FitMode;
import com.sitepark.ies.aggregator.value.media.scaling.Format;
import com.sitepark.ies.aggregator.value.media.scaling.ScaleImageRequest;
import com.sitepark.ies.aggregator.value.media.scaling.ScaledImage;
import java.util.List;

/**
 * Scales a source image into a set of output targets.
 *
 * <p>This is the port for the legacy SPML tag {@code sp:scaleimage}: it performs the actual image
 * scaling/cropping/padding (or, in edit mode, produces a URL that scales on demand). All targets of
 * one source image are scaled together as a batch, so the adapter can cache and optimize them
 * jointly.
 *
 * <p>The {@link ComputedSize#fitMode()} of each target selects how the image is fitted (see {@link
 * FitMode}), which the adapter maps to the engine's crop/fit/pad behavior:
 *
 * <ul>
 *   <li>{@link FitMode#COVER}: crop to the target size, placing the crop by the image's focal point;
 *       never pad, so a source too small to cover the target stays smaller.
 *   <li>{@link FitMode#CONTAIN}: scale down to fit inside the target size; never crop, never pad.
 *   <li>{@link FitMode#PAD}: fit inside the target size and fill the remainder with the target's
 *       background color; never crop.
 *   <li>{@link FitMode#COVER_OR_PAD}: crop while the source can cover the target, pad otherwise;
 *       always yields the exact target size.
 * </ul>
 *
 * <p>The {@link ScaleImageRequest#formats() requested formats} apply to every target, because the
 * engine writes all formats of a target from one scale operation. An adapter produces what it can and
 * reports it per target in {@link ScaledImage#encodings()} — consumers must read the {@link
 * Encoded#format() produced format} rather than assume the requested one was honored. Which {@link
 * Format} an adapter supports, and whether a format can be forced independently of the source
 * image's own format, is an adapter concern.
 */
public interface ImageScaler {

  /**
   * Scales the source image into all requested targets.
   *
   * @param request the source image, output formats and output targets to produce
   * @return one {@link ScaledImage} per requested target, in the same order as {@link
   *     ScaleImageRequest#targets()}; an empty list if the source image cannot be scaled
   */
  List<ScaledImage> scale(ScaleImageRequest request);
}
