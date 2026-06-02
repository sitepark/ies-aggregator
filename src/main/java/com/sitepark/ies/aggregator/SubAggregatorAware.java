package com.sitepark.ies.aggregator;

import java.util.Map;

/**
 * Optional capability for {@link Aggregator}s that compose other aggregators hierarchically.
 *
 * <p>The orchestrator hands in the named sub-aggregators after {@link Configurable#configure} and
 * before the call to {@link Aggregator#aggregate}.
 */
public interface SubAggregatorAware {

  /**
   * Set named sub-aggregators that this aggregator may delegate to.
   *
   * @param subAggregators map of named sub-aggregators
   */
  void setSubAggregators(Map<String, Aggregator> subAggregators);
}
