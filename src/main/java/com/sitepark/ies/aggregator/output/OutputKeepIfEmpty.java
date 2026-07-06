package com.sitepark.ies.aggregator.output;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a value (by type) — or a domain-object property — to be kept in the output even when empty.
 *
 * <p>By default empty values are dropped: the {@link OutputVisitor} omits any value that {@link
 * OutputVisitor#rendersEmpty(Object) renders empty} while traversing the output tree — a {@code
 * null}, an empty string/collection/map/array, an empty {@link
 * com.sitepark.ies.aggregator.value.Emptiable}, or a container whose children are all empty.
 *
 * <p>This annotation is the opt-out:
 *
 * <ul>
 *   <li><b>On a type</b> — the visitor never treats an instance of that type as empty (checked on
 *       the value's runtime class), so it is always rendered.
 *   <li><b>On a record component, field or accessor</b> — intended for a {@link DomainObjectMapper}
 *       to keep that specific empty property while unwrapping a domain object, so a single empty
 *       field nested inside an object can be retained.
 * </ul>
 *
 * <p>Non-empty values are always kept regardless of this annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface OutputKeepIfEmpty {}
