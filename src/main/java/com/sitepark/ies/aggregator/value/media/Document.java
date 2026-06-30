package com.sitepark.ies.aggregator.value.media;

import org.jspecify.annotations.Nullable;

/**
 * A document media asset (e.g. PDF), carrying the extracted textual content.
 *
 * <p>{@code extractedContent} is the plain text extracted from the document (e.g. for full-text
 * indexing), or {@code null} if no text could be extracted.
 */
public record Document(
    int id,
    String filename,
    String originFilename,
    String mimeType,
    long fileSize,
    Hash hash,
    GenericMetadata metadata,
    @Nullable String extractedContent)
    implements Media {}
