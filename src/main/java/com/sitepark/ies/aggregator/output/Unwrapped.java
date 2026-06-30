package com.sitepark.ies.aggregator.output;

import org.jspecify.annotations.Nullable;

/**
 * Carrier for properties that should be inlined ("unwrapped") into the surrounding object instead
 * of being rendered as a nested object — analogous to Jackson's {@code @JsonUnwrapped}.
 *
 * <p>When the {@link OutputVisitor} encounters an {@code Unwrapped} as a field value, it does not
 * emit a nested object under that field's key. Instead it resolves the carried value's properties
 * (via the configured {@link DomainObjectMapper}, or directly if the value already is a {@link
 * java.util.Map}) and dispatches each of them as a sibling field at the current object level.
 */
public record Unwrapped(@Nullable Object value) {
  /** An empty carrier that contributes no fields. */
  public static final Unwrapped EMPTY = new Unwrapped(null);
}
