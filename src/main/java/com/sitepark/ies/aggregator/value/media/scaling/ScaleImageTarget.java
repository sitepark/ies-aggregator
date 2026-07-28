package com.sitepark.ies.aggregator.value.media.scaling;

import org.jspecify.annotations.Nullable;

/**
 * A single output target to produce for a source image within a {@link ScaleImageRequest}.
 *
 * @param size the target size to scale to
 * @param backgroundColor the padding background color as a hex string without a leading {@code #}
 *     (e.g. {@code "FFFFFF"}), or {@code null} to let the adapter apply its default ({@code
 *     FFFFFF}); only relevant when the size's padding is {@link Padding#YES} or {@link Padding#FIT}
 */
public record ScaleImageTarget(ComputedSize size, @Nullable String backgroundColor) {

  /** Creates a scale target. */
  public static ScaleImageTarget of(ComputedSize size, @Nullable String backgroundColor) {
    return new ScaleImageTarget(size, backgroundColor);
  }
}
