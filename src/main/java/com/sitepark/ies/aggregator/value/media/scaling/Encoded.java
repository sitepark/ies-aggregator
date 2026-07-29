package com.sitepark.ies.aggregator.value.media.scaling;

import com.sitepark.ies.aggregator.value.uri.Uri;

/**
 * One encoding of a {@link ScaledImage}: the same geometry, written in one output format.
 *
 * <p>The scaling engine produces all encodings of a target from a single scale operation, so they
 * differ only in format and URL. The format is the one that was <b>actually</b> produced, which may
 * differ from a requested format the engine could not honor.
 *
 * @param format the format this encoding was written in
 * @param url the URL under which this encoding is available
 */
public record Encoded(Format format, Uri url) {

  /** Creates an encoding. */
  public static Encoded of(Format format, Uri url) {
    return new Encoded(format, url);
  }
}
