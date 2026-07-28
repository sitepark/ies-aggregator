package com.sitepark.ies.aggregator.port;

import com.sitepark.ies.aggregator.value.media.scaling.ComputedSize;
import com.sitepark.ies.aggregator.value.media.scaling.Padding;
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
 * <p>The {@link ComputedSize#padding()} of each target selects how the image is fitted (see {@link
 * Padding}), which the adapter maps to the engine's crop/fit/pad behavior:
 *
 * <ul>
 *   <li>{@link Padding#NO}: crop to the target size, placing the crop by the request's focal point.
 *   <li>{@link Padding#YES}: fit proportionally and fill the remainder with the target's background
 *       color; no crop.
 *   <li>{@link Padding#FIT}: like {@code YES} but forces the exact target size.
 * </ul>
 */
public interface ImageScaler {

  /**
   * Scales the source image into all requested targets.
   *
   * @param request the source image, focal point and output targets to produce
   * @return one {@link ScaledImage} per requested target, in the same order as {@link
   *     ScaleImageRequest#targets()}; an empty list if the source image cannot be scaled
   */
  List<ScaledImage> scale(ScaleImageRequest request);
}
