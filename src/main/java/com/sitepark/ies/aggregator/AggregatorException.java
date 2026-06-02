package com.sitepark.ies.aggregator;

/**
 * Signals that an {@link Aggregator} encountered an error while building resource data.
 */
public class AggregatorException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /**
   * @param message description of the error
   */
  public AggregatorException(String message) {
    super(message);
  }

  /**
   * @param message description of the error
   * @param cause the underlying cause
   */
  public AggregatorException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param cause the underlying cause
   */
  public AggregatorException(Throwable cause) {
    super(cause);
  }
}
