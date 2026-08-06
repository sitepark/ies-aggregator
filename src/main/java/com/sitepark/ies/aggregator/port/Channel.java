package com.sitepark.ies.aggregator.port;

import com.sitepark.ies.aggregator.value.uri.PlainUri;
import com.sitepark.ies.aggregator.value.uri.UriTarget;
import java.util.Optional;

/**
 * A publication channel through which CMS objects are accessible via URL.
 *
 * <p>Instances are channel-bound: every method answers for <em>this</em> channel. An injected {@code
 * Channel} is always the currently active one, which covers the common case without a lookup. Other
 * channels are obtained from {@link ChannelProvider}.
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
}
