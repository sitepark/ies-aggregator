package com.sitepark.ies.aggregator.value.media;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

/** Metadata for media kinds without kind-specific descriptive data. */
public record GenericMetadata(
    @Nullable String alternativeText,
    @Nullable String copyright,
    @Nullable String title,
    @Nullable String description,
    @Nullable Instant lastModified)
    implements Metadata {}
