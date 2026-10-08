package com.sitepark.ies.aggregator.port;

/**
 * How a channel finds an object that is not published in it - the {@code urlLookup} a channel
 * nature configures.
 *
 * <p>A channel that is fed from another one (a citizen's service portal, say) may link to objects it
 * does not publish itself, through the channel those objects are primarily published in ({@link
 * ChannelProvider#primary(int)}).
 */
public enum UrlLookup {

  /** An object not published in this channel is always looked up in its primary channel. */
  STRICT,

  /**
   * An object not published in this channel is looked up in its primary channel only when no
   * address was resolved for it otherwise.
   */
  ALTERNATIVE
}
