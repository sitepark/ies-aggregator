package com.sitepark.ies.aggregator.value.media;

/**
 * A media asset (e.g. image, document, audio, video) referenced from CMS source data.
 *
 * <p>Sealed over the concrete media kinds {@link Image}, {@link Document}, {@link Audio} and {@link
 * Video}; the common metadata is exposed here, while kind-specific data lives on each
 * implementation.
 */
public sealed interface Media permits Image, Document, Audio, Video {

  /** Returns the unique id of the media asset. */
  int id();

  /**
   * Returns the file name of the media asset, made URL-conform (and possibly adjusted from the
   * {@link #originFilename() original} for that reason).
   */
  String filename();

  /** Returns the original file name, before any URL-conformance adjustment. */
  String originFilename();

  /** Returns the MIME type of the media asset (e.g. {@code image/png}). */
  String mimeType();

  /** Returns the file size of the media asset in bytes. */
  long fileSize();

  /** Returns the content hash of the media asset. */
  Hash hash();

  /** Returns the descriptive metadata of the media asset. */
  Metadata metadata();
}
