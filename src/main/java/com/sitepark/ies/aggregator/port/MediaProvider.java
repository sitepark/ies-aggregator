package com.sitepark.ies.aggregator.port;

import com.sitepark.ies.aggregator.resolver.Resolver;
import com.sitepark.ies.aggregator.value.media.Media;
import java.util.Optional;

/** Loads a {@link Media} asset from the data exposed by a {@link Resolver}. */
public interface MediaProvider {

  /**
   * Reads the media data from the given resolver and builds a {@link Media} object.
   *
   * @param resolver the read-only view of the media source data
   * @return the resolved media, or empty if no media can be determined
   */
  Optional<Media> resolveMedia(Resolver resolver);
}
