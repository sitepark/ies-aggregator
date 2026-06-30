package com.sitepark.ies.aggregator.port;

import com.sitepark.ies.aggregator.resolver.Resolver;
import com.sitepark.ies.aggregator.value.media.MediaGroup;
import java.util.Optional;

/** Loads the {@link MediaGroup} settings of the group a media asset belongs to. */
public interface MediaGroupProvider {

  /**
   * Reads the group settings for the media described by the given resolver.
   *
   * @param resolver the read-only view of the media source data
   * @return the resolved media group settings, or empty if none can be determined
   */
  Optional<MediaGroup> resolveGroup(Resolver resolver);
}
