package com.sitepark.ies.aggregator.resolver;

/**
 * Factory that creates a fresh {@link Resolver} per aggregation run.
 *
 * <p>Each call to {@link #create(int)} produces a new instance, ensuring that mutable aggregator
 * state does not leak between generations.
 */
public interface RootResolverFactory {

  /**
   * Creates a new {@link Resolver} for the given object type.
   *
   * @param id of the article to aggregate the resource for
   * @return a fresh ResolverContext instance
   */
  Resolver create(int id);
}
