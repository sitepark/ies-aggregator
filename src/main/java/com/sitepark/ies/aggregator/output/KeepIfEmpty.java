package com.sitepark.ies.aggregator.output;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a domain-object property (or an entire type) to be kept in the output even when its value
 * is empty, as determined by {@link com.sitepark.ies.aggregator.value.Emptiable#isEmpty(Object)}.
 *
 * <p>By default a {@link DomainObjectMapper} drops empty properties while unwrapping a domain
 * object into its property map. This annotation is the opt-out: an annotated (empty) property is
 * retained. Non-empty properties are always kept regardless of this annotation.
 *
 * <p>Placing it on a type (e.g. a record) keeps every empty property of that type; placing it on a
 * single record component, field or accessor keeps only that property.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface KeepIfEmpty {}
