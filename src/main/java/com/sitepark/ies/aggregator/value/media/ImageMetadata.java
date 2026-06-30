package com.sitepark.ies.aggregator.value.media;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

/** Image-specific metadata, adding a {@link FocalPoint} to the shared fields. */
public record ImageMetadata(
    @Nullable String alternativeText,
    @Nullable String copyright,
    @Nullable String title,
    @Nullable String description,
    @Nullable Instant lastModified,
    FocalPoint focalPoint)
    implements Metadata {}
