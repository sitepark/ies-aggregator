package com.sitepark.ies.aggregator.port;

import com.sitepark.ies.aggregator.value.Channel;
import java.util.Optional;

/** Provides access to the configured {@link Channel} instances. */
public interface ChannelProvider {

  /** Returns the primary (default) publication channel. */
  Optional<Channel> primary(int id);

  /** Returns the currently active publication channel. */
  Optional<Channel> current();

  /**
   * Returns the publication channel with the given ID.
   *
   * @param id the channel ID
   * @return the channel
   */
  Optional<Channel> get(int id);
}
