package com.sitepark.ies.aggregator;

/**
 * Strategy interface for project-specific error handling during aggregation.
 *
 * <p>Implementations decide whether to abort processing, log the failure, or apply a fallback when
 * an {@link AggregatorException} is thrown.
 */
public interface AggregatorErrorHandler {

  /**
   * Handles an exception thrown by an aggregator.
   *
   * @param e the exception to handle
   */
  void handle(AggregatorException e);
}
