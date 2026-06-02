package com.sitepark.ies.aggregator;

import com.sitepark.ies.aggregator.output.OutputNode;
import com.sitepark.ies.aggregator.resolver.Resolver;

/**
 * Core strategy interface for aggregating data from a {@link Resolver} into an {@link OutputNode}.
 *
 * <p>Aggregators are instantiated once per configuration unit. An implementation that needs
 * lifecycle hooks opts in by implementing one or more of the optional capability interfaces:
 *
 * <ul>
 *   <li>{@link SubAggregatorAware} — for hierarchical composition with sub-aggregators
 *   <li>{@link Configurable} — for typed {@link Configuration}
 * </ul>
 *
 * <p>The orchestrator invokes the lifecycle hooks (when present) in the order listed above before
 * the call to {@link #aggregate}.
 */
public interface Aggregator {

  /**
   * Aggregates data from {@code source} into {@code output}.
   *
   * @param source the resolver providing CMS data
   * @param output the output node to write into
   * @throws AggregatorException if aggregation fails
   */
  void aggregate(Resolver source, OutputNode output) throws AggregatorException;
}
