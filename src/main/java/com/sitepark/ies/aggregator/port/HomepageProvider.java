package com.sitepark.ies.aggregator.port;

import com.sitepark.ies.aggregator.resolver.EntityResolver;
import java.util.List;

/**
 * Finds the home pages of the installation.
 *
 * <p>An object type the configuration marks as {@code home} is the start page of the group it lies
 * in - of a site or of a microsite. A site search offers one facet per home page, so it needs all of
 * them, wherever in the content tree they are. Neither the current object nor a link leads there;
 * only the platform, which can query the whole repository, can answer.
 *
 * <p>Each home page comes back as a root {@link EntityResolver}; its group is the last entry of
 * {@link EntityResolver#parentGroupPath()}. Ordering and the split into sites and microsites are the
 * caller's business.
 */
@FunctionalInterface
public interface HomepageProvider {

  /**
   * Returns every entity of the installation whose object type is configured as a home page.
   *
   * @return the home pages, in no particular order; empty if the installation has none
   */
  List<EntityResolver> homepages();
}
