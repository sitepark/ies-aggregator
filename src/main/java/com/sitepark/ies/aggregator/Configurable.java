package com.sitepark.ies.aggregator;

/**
 * Optional capability for {@link Aggregator}s that accept typed configurations.
 *
 * @param <C> the configuration type
 */
public interface Configurable<C extends Configuration> {

  /**
   * Applies typed configurations to this aggregator.
   *
   * @param config the typed configurations
   */
  void configure(C config);
}
