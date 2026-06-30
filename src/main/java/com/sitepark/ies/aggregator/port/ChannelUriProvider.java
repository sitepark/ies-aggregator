package com.sitepark.ies.aggregator.port;

import com.sitepark.ies.aggregator.value.Channel;
import com.sitepark.ies.aggregator.value.uri.PlainUri;
import java.util.Optional;

/** Resolves the URI under which an object is accessible through a given {@link Channel}. */
public interface ChannelUriProvider {

  /**
   * Resolves the URI for the given object within the publication channel.
   *
   * @param channel the publication channel
   * @param objectId the object to resolve the URI for
   * @return the resolved URI, or empty if no URI can be determined
   */
  Optional<PlainUri> resolveUri(Channel channel, int objectId);
}
