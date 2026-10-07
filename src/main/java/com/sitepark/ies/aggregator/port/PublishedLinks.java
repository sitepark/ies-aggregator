package com.sitepark.ies.aggregator.port;

import com.sitepark.ies.aggregator.resolver.Resolver;

/**
 * Tells the publisher which external URLs the resource being aggregated links to.
 *
 * <p>A side channel, not part of the output: the publisher keeps these URLs per published object
 * and channel, and a link checker reads them from there. Nothing an aggregator reports here shows
 * up in the resource it builds, and nothing it builds is reported on its own - an aggregator that
 * puts an external link into its output reports it here as well.
 *
 * <p>A report names the node the link was read from, so a broken URL can be traced back to the
 * place an editor has to correct. Where no such node exists - a link inside a rich text, say - the
 * report belongs to the resource as a whole.
 *
 * <p>Only absolute URLs are meant: an implementation drops a URL it cannot read as one rather than
 * failing the aggregation. Reporting the same URL twice for the same node is harmless.
 */
public interface PublishedLinks {

  /**
   * Reports an external URL held by the node {@code scope} reads from.
   *
   * @param scope the resolver positioned on the node whose fields hold the link
   * @param url the absolute URL the link points to
   */
  void external(Resolver scope, String url);

  /**
   * Reports an external URL that belongs to the resource as a whole, not to one of its nodes.
   *
   * @param url the absolute URL the link points to
   */
  void external(String url);
}
