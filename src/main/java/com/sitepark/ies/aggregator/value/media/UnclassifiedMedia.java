package com.sitepark.ies.aggregator.value.media;

import org.jspecify.annotations.Nullable;

/**
 * A media asset the store could not sort into one of the other kinds.
 *
 * <p>Not an error and not an unknown file: its name, size, hash and MIME type are all there — only
 * the kind is missing, because the store classifies just images, documents, audio and video. A
 * PostScript file is the typical case. It carries no kind-specific data beyond the shared fields,
 * which is exactly what makes it usable: a download link needs nothing else.
 */
public record UnclassifiedMedia(
    int objectId,
    int id,
    String filename,
    String originFilename,
    String mimeType,
    long fileSize,
    Hash hash,
    GenericMetadata metadata,
    @Nullable Origin origin)
    implements Media {}
