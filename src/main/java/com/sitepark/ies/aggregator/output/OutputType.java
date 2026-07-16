package com.sitepark.ies.aggregator.output;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a polymorphic type discriminator for a value type, emitted as a synthetic property when a
 * {@link DomainObjectMapper} unwraps an instance into its property map.
 *
 * <p>Value types that share an output shape but must be told apart by consumers (e.g. the variants
 * of a rich-text entity or a link) carry a constant discriminator such as {@code
 * "html.richText.internalLink"}. Instead of modelling that discriminator as a record component that
 * every constructor has to pass through, annotate the type:
 *
 * <pre>{@code
 * @OutputType("html.richText.internalLink")
 * public record InternalLink(int start, int end, Link link, RichText inner) implements Entity {}
 * }</pre>
 *
 * <p>The mapper injects the discriminator as the <b>first</b> entry of the property map, under the
 * key given by {@link #key()} (default {@code "modelType"}) with the value of {@link #value()}.
 *
 * <p>This is a {@link DomainObjectMapper} concern, not a {@code AnnotationIntrospector} one: the
 * discriminator is a synthetic property with no backing record component or accessor, so a
 * Jackson-backed mapper honors it by reading the annotation off the value's runtime class directly
 * (like the type-level {@link OutputKeepIfEmpty}), not via a Jackson introspection hook. It lets
 * value classes express the discriminator without depending on {@code com.fasterxml.jackson}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OutputType {

  /**
   * The discriminator value, e.g. {@code "html.richText.internalLink"}.
   *
   * @return the discriminator value
   */
  String value();

  /**
   * The output key under which the discriminator is emitted.
   *
   * @return the output key, {@code "modelType"} by default
   */
  String key() default "modelType";
}
