package com.sitepark.ies.aggregator;

import java.util.Map;

/**
 * Optional capability for {@link Aggregator}s that compose other aggregators hierarchically.
 *
 * <p>The orchestrator hands in the named sub-aggregators after {@link Configurable#configure} and
 * before the call to {@link Aggregator#aggregate}.
 *
 * <p>A {@link RootAggregatorFactory} may implement it too, to build a page from the section types
 * of its object type's steps. The platform reads the steps, decides what is placed in the current
 * channel and creates the aggregators; it hands them in before {@link RootAggregatorFactory#create}
 * for the same object. The steps themselves stay with the platform: the page has no node of a
 * step, so the factory gets one flat sequence. Its keys are {@code <step>/<section type>} - the key
 * of the step in {@code steps} and of the section type in the step's {@code sectionTypes} -
 * because the same section type may stand in two steps; the map iterates in the order the section
 * types are placed in (the steps by {@code order}, within a step the section types by {@code
 * order}, ties in the order of the configuration). See {@link SubSectionTypesAware} for what is
 * placed.
 */
public interface SubAggregatorAware {

  /**
   * Set named sub-aggregators that this aggregator may delegate to.
   *
   * @param subAggregators map of named sub-aggregators
   */
  void setSubAggregators(Map<String, Aggregator> subAggregators);
}
