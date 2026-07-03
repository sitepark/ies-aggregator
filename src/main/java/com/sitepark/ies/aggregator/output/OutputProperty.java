package com.sitepark.ies.aggregator.output;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Overrides the output key of a domain-object property (record component, field or getter) when a
 * {@link DomainObjectMapper} unwraps the object into its property map.
 *
 * <p>By default the property name is derived from the record component / bean property name, so
 * this annotation is only needed when the desired output key differs from that name — e.g. a record
 * component {@code isStatic} that should appear as {@code "static"} in the output.
 *
 * <p>This is the Jackson-free counterpart to {@code @JsonProperty}: it lets value classes express
 * output renaming without depending on {@code com.fasterxml.jackson}. A Jackson-backed {@link
 * DomainObjectMapper} adapter can honor it via a custom {@code AnnotationIntrospector}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface OutputProperty {

  /**
   * The output key to use for the annotated property.
   *
   * @return the output key
   */
  String value();
}
