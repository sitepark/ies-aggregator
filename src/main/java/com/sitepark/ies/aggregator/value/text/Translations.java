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
 * translation service via {@link #fromIndexed(List, List, String, String)}. Use {@link #SOURCE} to
 * render the untranslated source language.
 *
 * <p>The target language comes in two forms: {@link #targetLang()}, the language code that
 * language-dependent parts are rendered with, and {@link #targetLocale()}, the language as the
 * channel configures it (e.g. {@code en_US_deepl}), which a document names as its own language.
 *
 * <p>The keying is by reference and independent of {@link TranslatableText}'s {@code equals}: an
 * {@link IdentityHashMap} compares keys by reference in any case. Two value-equal source texts at
 * different positions in the tree therefore remain separate slots and can be translated differently.
 * The table itself is a render-time context rather than a value, so its own {@code equals}/{@code
 * hashCode} are identity-based.
 *
 * <p>This type lives in the {@code value.text} package — next to {@link TranslatableText}, the
 * identity key it is built around — to avoid a package cycle with the collecting and rendering
 * packages that depend on it.
 */
// The IdentityHashMap usage is intentional (see class Javadoc), not the accidental misuse the
// Error Prone check guards against.
@SuppressWarnings("IdentityHashMapUsage")
public final class Translations {

  /** Renders the source language: no translations and no language prefix. */
  public static final Translations SOURCE = new Translations(null, null, new IdentityHashMap<>());

  private final @Nullable String targetLang;
  private final @Nullable String targetLocale;
  private final Map<TranslatableText, String> translations;

  private Translations(
      @Nullable String targetLang,
      @Nullable String targetLocale,
      Map<TranslatableText, String> translations) {
    this.targetLang = targetLang;
    this.targetLocale = targetLocale;
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
    return fromIndexed(nodes, targets, targetLang, targetLang);
  }

  /**
   * Builds a translation table by index correspondence, as {@link #fromIndexed(List, List,
   * String)} does, for a target language whose configured form differs from its language code.
   *
   * @param nodes the collected source texts, in order
   * @param targets the translated strings, in the same order
   * @param targetLang the language code of the translations (e.g. {@code "en"})
   * @param targetLocale the target language as the channel configures it, in the form of {@link
   *     java.util.Locale#toString()} (e.g. {@code "en_US_deepl"})
   * @return the translation table
   * @throws IllegalArgumentException if {@code nodes} and {@code targets} differ in size
   */
  public static Translations fromIndexed(
      List<TranslatableText> nodes, List<String> targets, String targetLang, String targetLocale) {
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
    return new Translations(targetLang, targetLocale, map);
  }

  /** Returns the target language code, or {@code null} for the source language. */
  public @Nullable String targetLang() {
    return this.targetLang;
  }

  /**
   * Returns the target language as the channel configures it, in the form of {@link
   * java.util.Locale#toString()} (e.g. {@code "en_US_deepl"}), or {@code null} for the source
   * language. Built without one, it is the {@link #targetLang() language code}.
   */
  public @Nullable String targetLocale() {
    return this.targetLocale;
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
        + "', targetLocale='"
        + this.targetLocale
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
