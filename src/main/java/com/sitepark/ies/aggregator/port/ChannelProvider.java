package com.sitepark.ies.aggregator.port;

import java.util.Optional;

/**
 * Provides access to the configured {@link Channel} instances.
 *
 * <p>Needed to reach a channel other than the current one: obtain a {@link Channel}, then ask it
 * whether an object is published in it and under which URI it is accessible. For the current channel
 * alone, injecting a {@link Channel} directly is enough.
 */
public interface ChannelProvider {

  /**
   * The currently active publication channel.
   *
   * <p>The same channel an injected {@link Channel} refers to.
   *
   * @return the current channel; never {@code null}
   */
  Channel current();

  /**
   * The primary (default) publication channel of the given object.
   *
   * @param objectId the id of the object
   * @return the object's primary channel, or empty if the object has none
   */
  Optional<Channel> primary(int objectId);

  /**
   * The publication channel with the given id.
   *
   * @param channelId the channel id
   * @return the channel, or empty if no channel with that id is configured
   */
  Optional<Channel> get(int channelId);
}
