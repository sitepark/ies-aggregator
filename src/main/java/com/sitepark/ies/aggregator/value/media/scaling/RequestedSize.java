package com.sitepark.ies.aggregator.value.media.scaling;

import org.jspecify.annotations.Nullable;

/**
 * A single requested output size for an image.
 *
 * <p>Either dimension may be omitted: if only one of {@code width}/{@code height} is given together
 * with an {@code aspectRatio}, the missing dimension is derived from the aspect ratio. If both are
 * given and no aspect ratio is set, the aspect ratio is derived from them.
 *
 * @param width the requested width in pixels, or {@code null} if unspecified
 * @param height the requested height in pixels, or {@code null} if unspecified
 * @param aspectRatio the requested aspect ratio, or {@code null} to inherit the root aspect ratio
 * @param padding the requested padding mode, or {@code null} to inherit the root padding
 * @param blank whether a blank (transparent 1×1) placeholder is requested instead of a real scaling
 */
public record RequestedSize(
    @Nullable Integer width,
    @Nullable Integer height,
    @Nullable AspectRatio aspectRatio,
    @Nullable Padding padding,
    boolean blank) {

  /** Creates a requested size. */
  public static RequestedSize of(
      @Nullable Integer width,
      @Nullable Integer height,
      @Nullable AspectRatio aspectRatio,
      @Nullable Padding padding,
      boolean blank) {
    return new RequestedSize(width, height, aspectRatio, padding, blank);
  }
}
