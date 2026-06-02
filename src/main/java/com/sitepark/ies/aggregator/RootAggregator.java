package com.sitepark.ies.aggregator;

import com.sitepark.ies.aggregator.output.OutputObject;
import com.sitepark.ies.aggregator.resolver.Resolver;
import java.util.List;

/**
 * Orchestrates a sequence of {@link Aggregator} instances to build a complete {@link OutputObject}.
 *
 * <p>Each aggregator in the list is invoked in order and writes its contribution into the same
 * shared {@link OutputObject}. The result is returned after all aggregators have run.
 */
public class RootAggregator {

  private final List<Aggregator> aggregators;

  /**
   * @param aggregators the ordered list of aggregators to apply
   */
  public RootAggregator(List<Aggregator> aggregators) {
    this.aggregators = List.copyOf(aggregators);
  }

  /**
   * Runs all aggregators against the given resolver and returns the assembled output.
   *
   * @param resolverContext the data source for all aggregators
   * @return the assembled output object
   * @throws AggregatorException if any aggregator fails
   */
  public OutputObject aggregate(Resolver resolverContext) throws AggregatorException {
    OutputObject content = new OutputObject(null, null);
    for (Aggregator aggregator : aggregators) {
      aggregator.aggregate(resolverContext, content);
    }
    return content;
  }
}
