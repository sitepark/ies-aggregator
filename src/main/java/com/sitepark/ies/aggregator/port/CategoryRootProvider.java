package com.sitepark.ies.aggregator.port;

import com.sitepark.ies.aggregator.resolver.EntityResolver;
import java.util.List;

/**
 * Finds the category roots of the installation.
 *
 * <p>A category whose parent category is not set is the root of a category tree. A search form
 * offers one filter per root, and an editor's choice for a root is stored under field names that
 * carry the root's id - so the aggregator needs every root there is, linked or not. Neither the
 * current object nor a link leads there; only the platform, which can query the whole repository,
 * can answer.
 *
 * <p>Each root comes back as a root {@link EntityResolver}, whether the channel publishes it or not.
 * Ordering is the caller's business.
 */
@FunctionalInterface
public interface CategoryRootProvider {

  /**
   * Returns every category of the installation that has no parent category.
   *
   * @return the category roots, in no particular order; empty if the installation has none
   */
  List<EntityResolver> categoryRoots();
}
