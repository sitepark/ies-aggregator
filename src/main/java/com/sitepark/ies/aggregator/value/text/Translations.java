package com.sitepark.ies.aggregator.value.text;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * An immutable, per-language translation table: maps {@link TranslatableText} instances to their
 * translated string, keyed by object identity.
 *
 * <p>The table is a render-time view over an unmodified output tree. A translation-aware writer
 * looks each {@link TranslatableText} up via {@link #translationFor(TranslatableText)} and uses
 * {@link #targetLang()} to render language-dependent parts (e.g. the path prefix of a {@link
 * com.sitepark.ies.aggregator.value.uri.TranslatableUri TranslatableUri}). Because translations
 * live here and not in the tree, the same tree can be
 * rendered into any number of languages — even concurrently.
 *
 * <p>Build a table from the collected texts and the index-corresponding translations returned by a
 * translation service via {@link #fromIndexed(List, List, String)}. Use {@link #SOURCE} to render
 * the untranslated source language.
 *
 * <p>This type lives in the {@code value.text} package — next to {@link TranslatableText}, the
 * identity key it is built around — to avoid a package cycle with the collecting and rendering
 * packages that depend on it.
 *
 * <p>Because this table is keyed by object identity (an {@link IdentityHashMap} over {@link
 * TranslatableText} instances), {@code equals}/{@code hashCode} are deliberately
 * <strong>identity-based</strong> (reference equality), not value-based: value-based equality would
 * be meaningless here.
 */
// Identity-keyed by design: TranslatableText instances are looked up by reference, not value
// (see class Javadoc). The IdentityHashMap usage is intentional, not the accidental misuse the
// Error Prone check guards against.
@SuppressWarnings("IdentityHashMapUsage")
public final class Translations {

  /** Renders the source language: no translations and no language prefix. */
  public static final Translations SOURCE = new Translations(null, new IdentityHashMap<>());

  private final @Nullable String targetLang;
  private final Map<TranslatableText, String> translations;

  private Translations(@Nullable String targetLang, Map<TranslatableText, String> translations) {
    this.targetLang = targetLang;
    this.translations = new IdentityHashMap<>(translations);
  }

  /**
   * Builds a translation table by index correspondence: {@code nodes.get(i)} is mapped to {@code
   * targets.get(i)}. The {@code nodes} list is the ordered bridge produced by {@code
   * TranslatableTextCollector.collect}; {@code targets} are the translations returned by the
   * translation service in the same order.
   *
   * @param nodes the collected source texts, in order
   * @param targets the translated strings, in the same order
   * @param targetLang the language code of the translations (e.g. {@code "de"})
   * @return the translation table
   * @throws IllegalArgumentException if {@code nodes} and {@code targets} differ in size
   */
  public static Translations fromIndexed(
      List<TranslatableText> nodes, List<String> targets, String targetLang) {
    if (nodes.size() != targets.size()) {
      throw new IllegalArgumentException(
          "nodes and targets must have the same size, but were "
              + nodes.size()
              + " and "
              + targets.size());
    }
    Map<TranslatableText, String> map = new IdentityHashMap<>(nodes.size());
    for (int i = 0; i < nodes.size(); i++) {
      map.put(nodes.get(i), targets.get(i));
    }
    return new Translations(targetLang, map);
  }

  /** Returns the target language code, or {@code null} for the source language. */
  public @Nullable String targetLang() {
    return this.targetLang;
  }

  /**
   * Returns the translation for the given text, or its source text if none is registered.
   *
   * @param text the text to look up
   * @return the translated string, or {@code text.getSourceText()} as a fallback
   */
  public String translationFor(TranslatableText text) {
    return this.translations.getOrDefault(text, text.getSourceText());
  }

  @Override
  public String toString() {
    return "Translations{targetLang='"
        + this.targetLang
        + "', size="
        + this.translations.size()
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    // Identity-based by design: see class Javadoc.
    return this == o;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }
}
