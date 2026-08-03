package com.sitepark.ies.aggregator.value.media.scaling;

import com.sitepark.ies.aggregator.output.OutputProperty;

/** Aspect ratio of an image, optionally adapting dynamically. */
public record AspectRatio(
    @OutputProperty("aspectRatioX") int x,
    @OutputProperty("aspectRatioY") int y,
    @OutputProperty("dynamicAspectRatio") boolean dynamic) {

  /** Creates an aspect ratio. */
  public static AspectRatio of(int x, int y, boolean dynamic) {
    return new AspectRatio(x, y, dynamic);
  }
}
