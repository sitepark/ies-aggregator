package com.sitepark.ies.aggregator.output;

import org.jspecify.annotations.Nullable;

/**
 * Wrapper that flags a single value to be kept in the output even when it is empty.
 *
 * <p>By default the {@link OutputVisitor} drops empty values while rendering (see {@link
 * OutputVisitor#rendersEmpty(Object)}). A value that is decided — per property — to be kept even
 * when empty cannot express that through its own type, because whether it should be retained depends
 * on <em>where</em> it sits, not on <em>what</em> it is. This wrapper carries that per-occurrence
 * decision to the visitor:
 *
 * <ul>
 *   <li>{@link OutputVisitor#rendersEmpty(Object)} never treats a {@code KeepEmpty} as empty, so it
 *       is never dropped.
 *   <li>{@link OutputVisitor#visitField(String, Object)} unwraps it and dispatches the inner value,
 *       so the wrapper is transparent to every format visitor.
 * </ul>
 *
 * <p>A {@link DomainObjectMapper} produces these wrappers for properties annotated with {@link
 * OutputKeepIfEmpty} (property level); the type-level opt-out is handled directly by the visitor via
 * the value's runtime class and needs no wrapper.
 *
 * @param value the wrapped value, rendered as-is (may itself be {@code null} or empty)
 */
public record KeepEmpty(@Nullable Object value) {}
