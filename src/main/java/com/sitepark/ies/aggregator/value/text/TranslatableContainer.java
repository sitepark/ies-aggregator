package com.sitepark.ies.aggregator.value.text;

import java.util.List;

/**
 * Implemented by value objects that contain one or more {@link TranslatableText} references and
 * render themselves against a {@link Translations} table at render time.
 *
 * <p>{@link #getTranslatableTextList()} is the <em>collection</em> side: callers — typically {@link
 * com.sitepark.ies.aggregator.output.collect.TranslatableTextCollector TranslatableTextCollector} —
 * gather all translatable texts from composite values. It is a pure query with no side-effects.
 *
 * <p>{@link #render(Translations)} is the <em>rendering</em> side: given the per-language
 * translation table, the container produces its output. The return type is {@link Object} so a
 * container may render either a scalar (e.g. a {@code String}, like {@code TranslatableSplitText}
 * and {@code TranslatableUri}) or a structured value (an output object/map); the {@link
 * com.sitepark.ies.aggregator.output.OutputVisitor OutputVisitor} re-dispatches the result.
 */
public interface TranslatableContainer {

  /**
   * Returns all contained {@link TranslatableText} instances.
   *
   * @return the translatable texts, or an empty list if none
   */
  default List<TranslatableText> getTranslatableTextList() {
    return List.of();
  }

  /**
   * Renders this container against the given translation table.
   *
   * @param translations the translation table (use {@link Translations#SOURCE} for the source
   *     language)
   * @return the rendered value — a scalar (e.g. {@code String}) or a structured output value
   */
  Object render(Translations translations);
}
