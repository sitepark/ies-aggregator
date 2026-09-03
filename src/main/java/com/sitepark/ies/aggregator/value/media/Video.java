package com.sitepark.ies.aggregator.value.media;

import org.jspecify.annotations.Nullable;

/** A video media asset, carrying its pixel dimensions. */
public record Video(
    int objectId,
    int id,
    String filename,
    String originFilename,
    String mimeType,
    long fileSize,
    Hash hash,
    GenericMetadata metadata,
    @Nullable Origin origin,
    int width,
    int height)
    implements Media {}
