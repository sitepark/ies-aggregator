package com.sitepark.ies.aggregator.value.media;

import com.sitepark.ies.aggregator.value.uri.PlainUri;

/**
 * A zip archive the publisher has written for the resource being aggregated, as the aggregator
 * links it: the legacy {@code sp:zip} returns the same three facts as a medium.
 *
 * @param filename the file name of the archive, {@code <name>.zip}
 * @param url the URL of the archive, a path into the media directory of the published object
 * @param size the size of the archive in bytes
 */
public record ZipArchive(String filename, PlainUri url, long size) {

  /** The MIME type of every archive. */
  public static final String MIME_TYPE = "application/zip";

  /**
   * The file name an archive of the given name gets: every character other than a letter of the
   * ASCII alphabet, a digit, a dot or a hyphen becomes an underscore, and {@code .zip} follows.
   *
   * <p>The rule of {@code sp:zip}; it also keeps an umlaut out of the URL, so a name of "Rathaus
   * Süd" yields {@code Rathaus_S_d.zip}.
   *
   * @param name the name given to the archive
   * @return the file name
   */
  public static String filenameOf(String name) {
    return name.replaceAll("[^a-zA-Z0-9.\\-]", "_") + ".zip";
  }
}
