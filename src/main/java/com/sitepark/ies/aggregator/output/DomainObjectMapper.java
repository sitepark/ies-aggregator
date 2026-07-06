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
 * <p>The returned map must contain the original property values (not already-serialized
 * sub-representations) — otherwise typed value classes lose their identity and the visitor cannot
 * dispatch them correctly.
 *
 * <p>An implementation maps only <b>structure</b>: property renaming and flat inlining are
 * expressed with the Jackson-free annotations {@link OutputProperty} and {@link OutputUnwrapped},
 * which it is expected to honor. It should return <b>all</b> properties (including empty ones) —
 * dropping empty values is the {@link OutputVisitor}'s responsibility (see {@link
 * OutputVisitor#rendersEmpty(Object)}), so emptiness is decided uniformly for every value in the
 * output tree, not just for domain objects.
 *
 * <p>The one emptiness-related duty of a mapper is to honor the <b>property-level</b> {@link
 * OutputKeepIfEmpty}: a property (record component, field or accessor) carrying it must be kept even
 * when empty. Because the visitor decides emptiness by value and cannot see the property, the mapper
 * signals this by wrapping such a property's value in {@link KeepEmpty}, which the visitor never
 * drops and unwraps transparently. (The type-level {@link OutputKeepIfEmpty} needs no wrapper — the
 * visitor checks it on the value's own runtime class.)
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
