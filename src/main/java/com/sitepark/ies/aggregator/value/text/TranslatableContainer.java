package com.sitepark.ies.aggregator.value.text;

import java.util.List;

/**
 * Implemented by value objects that contain one or more {@link TranslatableText} references.
 *
 * <p>Callers — typically
 * {@link com.sitepark.ies.aggregator.output.collect.TranslatableTextCollector
 * TranslatableTextCollector} — use this interface to collect all translatable texts from composite
 * values. The method is a pure query: it has no side-effects and may be called any number of times.
 * The target language is supplied at render time through a {@link Translations} table, not as a
 * command on the concrete type.
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
}
