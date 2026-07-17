package com.sitepark.ies.aggregator.output;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a domain-object property to be inlined flat into the surrounding object when a {@link
 * DomainObjectMapper} unwraps the object: the property's own key is dropped and the nested object's
 * properties take its place as sibling entries.
 *
 * <p>A {@code null} value contributes nothing — so, unlike a plain property, an unwrapped property
 * leaves no dangling key. The optional {@link #prefix()} / {@link #suffix()} are applied to the
 * inlined keys.
 *
 * <p>The value may also be a {@link java.util.Map} — whose entries are merged directly — or an
 * {@link java.lang.Iterable} (e.g. a {@code List}), in which case each element is inlined flat in
 * turn, recursively. A list slot therefore lets a value type carry <em>any number</em> of
 * independent extension objects that all render as siblings; an empty list contributes nothing, and
 * on a key collision the element inlined last wins.
 *
 * <p>This is the Jackson-free counterpart to {@code @JsonUnwrapped}: it lets value classes express
 * inlining without depending on {@code com.fasterxml.jackson}. A Jackson-backed {@link
 * DomainObjectMapper} adapter can honor it via a custom {@code AnnotationIntrospector}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface OutputUnwrapped {

  /**
   * Prefix applied to every inlined key.
   *
   * @return the key prefix, empty by default
   */
  String prefix() default "";

  /**
   * Suffix applied to every inlined key.
   *
   * @return the key suffix, empty by default
   */
  String suffix() default "";
}
