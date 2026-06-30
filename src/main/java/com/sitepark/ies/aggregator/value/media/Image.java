package com.sitepark.ies.aggregator.value.media;

/** A raster image media asset, carrying its pixel dimensions. */
public record Image(
    int id,
    String filename,
    String originFilename,
    String mimeType,
    long fileSize,
    Hash hash,
    ImageMetadata metadata,
    int width,
    int height)
    implements Media {}
