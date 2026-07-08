package com.sitepark.ies.aggregator.port;

import com.sitepark.ies.aggregator.value.Channel;
import com.sitepark.ies.aggregator.value.uri.PlainUri;
import com.sitepark.ies.aggregator.value.uri.UriTarget;
import java.util.Optional;

/** Resolves the URI under which a {@link UriTarget} is accessible through a given {@link Channel}. */
public interface ChannelUriProvider {

  /**
   * Resolves the URI for the given target within the publication channel.
   *
   * @param channel the publication channel
   * @param target what to resolve the URI for (e.g. a standalone object or an article media binary)
   * @return the resolved URI, or empty if no URI can be determined
   */
  Optional<PlainUri> resolveUri(Channel channel, UriTarget target);
}
