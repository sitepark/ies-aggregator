/**
 * Vocabulary and the {@link com.sitepark.ies.aggregator.port.ImageScaler ImageScaler} contract for
 * scaling an image into a set of output sizes.
 *
 * <p>A caller expresses the wanted output sizes as {@link
 * com.sitepark.ies.aggregator.value.media.scaling.RequestedSize RequestedSize}s (each with optional
 * width/height, {@link com.sitepark.ies.aggregator.value.media.scaling.AspectRatio AspectRatio} and
 * {@link com.sitepark.ies.aggregator.value.media.scaling.Padding Padding}). Turned into concrete
 * {@link com.sitepark.ies.aggregator.value.media.scaling.ComputedSize ComputedSize}s for a given
 * {@link com.sitepark.ies.aggregator.value.media.scaling.SourceImage SourceImage}, they are bundled
 * with a background color into {@link
 * com.sitepark.ies.aggregator.value.media.scaling.ScaleImageTarget ScaleImageTarget}s and passed as
 * one {@link com.sitepark.ies.aggregator.value.media.scaling.ScaleImageRequest ScaleImageRequest} to
 * an {@link com.sitepark.ies.aggregator.port.ImageScaler ImageScaler}, which yields one {@link
 * com.sitepark.ies.aggregator.value.media.scaling.ScaledImage ScaledImage} per target.
 */
@NullMarked
package com.sitepark.ies.aggregator.value.media.scaling;

import org.jspecify.annotations.NullMarked;
