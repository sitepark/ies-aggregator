package com.sitepark.ies.aggregator.value.media;

/** An audio media asset, carrying its playback duration. */
public record Audio(
    int id,
    String filename,
    String originFilename,
    String mimeType,
    long fileSize,
    Hash hash,
    GenericMetadata metadata)
    implements Media {}
