package com.sitepark.ies.aggregator.output;

import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Strategy for unwrapping a domain object (e.g. {@code Link}) into a {@code Map<String, Object>} of
 * its properties, preserving the original property values as Java objects.
 *
 * <p>The visitor calls this strategy for any value that is not one of the natively supported types
 * ({@link OutputObject}, {@link OutputList}, {@link OutputListItem}, value classes from {@code
 * com.sitepark.ies.aggregator.value}, primitives). If the mapper recognizes the value and returns a
 * non-{@code null} map, the visitor traverses the properties — so embedded {@code
 * TranslatableText}, {@code Uri}, etc. are dispatched to their typed {@code visit*} methods
 * automatically.
 *
 * <p>If the mapper does not recognize the value, it must return {@code null} so the visitor can
 * fall back to {@link OutputVisitor#visitUnknown(Object)}.
 *
 * <p>The returned map must contain the original property values (not Jackson-serialized
 * sub-representations) — otherwise typed value classes lose their identity and the visitor cannot
 * dispatch them correctly.
 */
@FunctionalInterface
public interface DomainObjectMapper {

  /**
   * Mapper that recognizes nothing. Used as the default in {@link OutputVisitor} when no mapper is
   * configured.
   */
  DomainObjectMapper NONE = value -> null;

  /**
   * Returns the properties of {@code value} as a map, or {@code null} if {@code value} is not a
   * domain object known to this mapper.
   *
   * @param value the value to unwrap; never {@code null}
   * @return the original property values keyed by property name, or {@code null}
   */
  @Nullable Map<String, Object> toProperties(Object value);
}
