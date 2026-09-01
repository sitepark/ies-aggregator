package com.sitepark.ies.aggregator.port;

import java.util.Optional;

/**
 * Reads the configuration of an object's type.
 *
 * <p>An object type is configured, not just named: which editing steps it offers, how it is
 * published, and whatever else the surrounding CMS distribution decides to attach to it. An
 * aggregator regularly has to know some of that — {@link
 * com.sitepark.ies.aggregator.resolver.EntityDescriptor#type()} answers <em>which</em> type an
 * object has, this port answers <em>how that type is configured</em>.
 *
 * <p>The shape of that configuration is not this API's business: every distribution defines its own
 * keys. The caller therefore names the type it wants the configuration bound to and the adapter
 * fills it — the same division of labour {@link
 * com.sitepark.ies.aggregator.value.StructuredValueParser} uses for embedded structured values.
 * Unknown keys are ignored, so a caller declares only the handful of settings it actually reads.
 */
@FunctionalInterface
public interface ObjectTypeConfigProvider {

  /**
   * Returns the configuration of the given object's type, bound to {@code type}.
   *
   * <p>Keyed by the object rather than by the type name: which configuration applies can depend on
   * where the object sits, and only the adapter knows that.
   *
   * @param objectId the id of the object whose type configuration is wanted
   * @param type the target type the configuration is bound to
   * @param <T> the target type
   * @return the bound configuration, or {@link Optional#empty()} if the object or its type
   *     configuration cannot be found
   */
  <T> Optional<T> configuration(int objectId, Class<T> type);
}
