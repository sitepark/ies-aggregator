package com.sitepark.ies.aggregator.value.media;

import com.sitepark.ies.aggregator.value.Emptiable;
import org.jspecify.annotations.Nullable;

/**
 * A media asset (e.g. image, document, audio, video) referenced from CMS source data.
 *
 * <p>A media asset never stands on its own: it is always used through a CMS object (page/article) —
 * either a media object whose content <em>is</em> the asset, or an object that embeds it. {@link
 * #objectId()} identifies that object, {@link #id()} the asset within it; together they address the
 * asset, see {@link com.sitepark.ies.aggregator.value.uri.UriTarget#ofMedia(int, int)}.
 *
 * <p>Sealed over the concrete media kinds {@link Image}, {@link Document}, {@link Audio} and {@link
 * Video}; the common metadata is exposed here, while kind-specific data lives on each
 * implementation. {@link EmptyMedia} completes the family as the {@link #empty() empty} media asset,
 * so a media field can be filled without {@code null}.
 */
public sealed interface Media extends Emptiable permits Image, Document, Audio, Video, EmptyMedia {

  /**
   * Returns the empty media asset, used where a media field references no asset.
   *
   * @return the single {@link EmptyMedia} instance
   */
  static EmptyMedia empty() {
    return EmptyMedia.INSTANCE;
  }

  /**
   * Returns {@code true} if this is the {@link #empty() empty} media asset.
   *
   * <p>Concrete media kinds are never empty; only {@link EmptyMedia} reports {@code true}.
   */
  @Override
  default boolean isEmpty() {
    return false;
  }

  /** Returns the id of the CMS object (page/article) the media asset was uploaded to. */
  int objectId();

  /** Returns the unique id of the media asset within its object. */
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

  /**
   * Returns where the asset was synchronised from, or {@code null} if it stems from no external
   * system.
   *
   * <p>Unlike the other accessors this one is nullable rather than neutral: an empty {@link Origin}
   * would claim a provenance the asset does not have.
   */
  @Nullable Origin origin();
}
