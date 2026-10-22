package com.sitepark.ies.aggregator.value.media;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

/**
 * Image-specific metadata, adding a {@link FocalPoint} to the shared fields.
 *
 * @param digitalSourceType what the image file itself declares about how it was made (IPTC {@code
 *     Iptc4xmpExt:DigitalSourceType}, as a term or as its IPTC URI), or {@code null} if it declares
 *     nothing
 */
public record ImageMetadata(
    @Nullable String alternativeText,
    @Nullable String copyright,
    @Nullable String title,
    @Nullable String description,
    @Nullable Instant lastModified,
    FocalPoint focalPoint,
    @Nullable String digitalSourceType)
    implements Metadata {}
