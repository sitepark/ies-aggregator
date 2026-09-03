package com.sitepark.ies.aggregator.value.media;

import org.jspecify.annotations.Nullable;

/**
 * A raster image media asset, carrying its pixel dimensions and the colors extracted from it — the
 * swatch is {@code null} if none were extracted.
 */
public record Image(
    int objectId,
    int id,
    String filename,
    String originFilename,
    String mimeType,
    long fileSize,
    Hash hash,
    ImageMetadata metadata,
    @Nullable Origin origin,
    int width,
    int height,
    @Nullable ColorSwatch colorSwatch)
    implements Media {}
