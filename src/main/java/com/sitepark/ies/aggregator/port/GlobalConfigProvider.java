package com.sitepark.ies.aggregator.port;

import java.util.Optional;

/**
 * Reads the global configuration of the installation.
 *
 * <p>Beside what an object type or a channel configures, a distribution keeps switches and defaults
 * that hold for the whole installation - whether a feature is enabled at all, the copyright notice
 * every image falls back to. A customer module overrides them, so their value is known only to the
 * platform that merges the configuration; the aggregator asks for one by name.
 *
 * <p>As with {@link ObjectTypeConfigProvider}, the shape is not this API's business: the caller names
 * the type it wants the value bound to and the adapter fills it. Unknown keys are ignored.
 */
@FunctionalInterface
public interface GlobalConfigProvider {

  /**
   * Returns the global configuration entry of the given name, bound to {@code type}.
   *
   * @param name the name of the entry below the global configuration, e.g. {@code
   *     aiContentLabeling}
   * @param type the target type the entry is bound to
   * @param <T> the target type
   * @return the bound entry, or {@link Optional#empty()} if the installation configures none
   */
  <T> Optional<T> configuration(String name, Class<T> type);
}
