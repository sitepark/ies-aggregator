package com.sitepark.ies.aggregator.port;

import java.time.Instant;

/**
 * Asks the publisher to generate the resource being aggregated again at a later moment.
 *
 * <p>Some output depends on time rather than on content: an entry whose display window opens next
 * week, or closes tomorrow, is right today and wrong then, although nothing was edited. An
 * aggregator that writes such output names the moment here, and the publisher regenerates the
 * resource at that moment.
 *
 * <p>Several calls for one resource are expected; the earliest moment that still lies in the future
 * wins. A moment in the past is ignored - it cannot be met any more. Like {@link PublishedLinks}
 * this is a side channel: nothing reported here shows up in the resource itself.
 */
public interface Regeneration {

  /**
   * Asks for the resource to be generated again at the given moment.
   *
   * @param moment when the output written now has to be renewed
   */
  void at(Instant moment);
}
