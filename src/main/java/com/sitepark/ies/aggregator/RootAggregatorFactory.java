package com.sitepark.ies.aggregator;

/**
 * Factory that creates a fresh {@link RootAggregator} per aggregation run.
 *
 * <p>Each call to {@link #create(int)} produces a new instance, ensuring that mutable aggregator
 * state does not leak between generations.
 */
public interface RootAggregatorFactory {

  /**
   * Creates a new {@link RootAggregator} for the given object type.
   *
   * @param id of the article to aggregate the resource for
   * @return a fresh generator instance
   */
  RootAggregator create(int id);
}
