package com.sitepark.ies.aggregator.port;

import com.sitepark.ies.aggregator.value.uri.PlainUri;
import com.sitepark.ies.aggregator.value.uri.UriTarget;
import java.util.Optional;

/**
 * A publication channel through which CMS objects are accessible via URL.
 *
 * <p>Instances are channel-bound: every method answers for <em>this</em> channel. The injected
 * instance is always the currently active channel; other channels are reached by navigation ({@link
 * #primary(int)}, {@link #get(int)}).
 */
public interface Channel {

  /**
   * The id of this channel.
   *
   * @return the channel id
   */
  int id();

  /**
   * The name of this channel.
   *
   * @return the channel name
   */
  String name();

  /**
   * Whether the object with the given id is published in this channel.
   *
   * @param objectId the id of the object to check
   * @return {@code true} if the object is published in this channel
   */
  boolean isPublished(int objectId);

  /**
   * Resolves the URI under which the given target is accessible in this channel.
   *
   * @param target what to resolve the URI for (e.g. a standalone object or an article media binary)
   * @return the resolved URI, or empty if no URI can be determined
   */
  Optional<PlainUri> resolveUri(UriTarget target);

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
