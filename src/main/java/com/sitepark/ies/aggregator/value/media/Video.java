package com.sitepark.ies.aggregator.value.media;

/** A video media asset, carrying its pixel dimensions. */
public record Video(
    int id,
    String filename,
    String originFilename,
    String mimeType,
    long fileSize,
    Hash hash,
    GenericMetadata metadata,
    int width,
    int height)
    implements Media {}
