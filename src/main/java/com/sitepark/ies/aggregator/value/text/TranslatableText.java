package com.sitepark.ies.aggregator.value.text;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * An immutable source text that can be translated.
 *
 * <p>Unlike {@link PlainText}, this type is used <em>as a translation key</em>: it is collected from
 * the output tree by a {@code TranslatableTextCollector} and serves as the key of an external
 * translation table ({@link Translations}). The instance itself only carries the source text and its
 * {@link Format}; the translation lives outside the tree.
 *
 * <p>{@code equals}/{@code hashCode} are <strong>value-based</strong> (source text and format), so
 * instances can be compared like any other value object. {@link Translations} does not rely on them
 * — it keys by reference — so two value-equal occurrences of the same source text remain separate
 * translation slots and may be translated differently depending on their position in the tree. For
 * that reason, never manage translation slots in a value-hashing collection ({@code HashMap}, {@code
 * HashSet}, {@code Stream.distinct()}): it would collapse them into one. Always go through {@link
 * Translations}.
 */
public final class TranslatableText implements Text {
  private final Format format;

  private final String sourceText;

  private TranslatableText(String sourceText, Format format) {
    this.sourceText = sourceText;
    this.format = format;
  }

  /**
   * Creates a translatable text with {@link Format#TEXT} format.
   *
   * @param sourceText the source text; {@code null} or blank is treated as empty
   */
  public static TranslatableText of(String sourceText) {
    return TranslatableText.of(sourceText, Format.TEXT);
  }

  @Override
  public PlainText toPlainText() {
    return PlainText.of(this.sourceText);
  }

  @Override
  public Text translatable() {
    return this;
  }

  /**
   * Creates a translatable text with the given format.
   *
   * @param sourceText the source text; {@code null} or blank is treated as empty
   * @param format the text format
   */
  public static TranslatableText of(String sourceText, Format format) {
    if (sourceText == null) {
      return new TranslatableText("", format);
    }
    if (sourceText.isBlank()) {
      return new TranslatableText("", format);
    }
    return new TranslatableText(sourceText, format);
  }

  /**
   * Returns an independent copy carrying the same source text and format but with a fresh identity.
   * Because {@link Translations} keys by identity, a copy is an independent translation slot: it can
   * be translated separately from the instance it was copied from.
   *
   * <p>The copy is <em>value-equal</em> to its origin — {@code equals} cannot tell them apart. Use
   * {@code ==} (or AssertJ's {@code isNotSameAs}) to distinguish translation slots.
   *
   * @return a copy with a new identity
   */
  public TranslatableText copy() {
    return new TranslatableText(this.sourceText, this.format);
  }

  /**
   * Returns a non-translatable {@link PlainText} carrying this instance's source text.
   *
   * <p>The {@link Format} is dropped — a {@code PlainText} is rendered verbatim and has no format —
   * and the resulting text is no longer collected into the translation table.
   *
   * @return a {@code PlainText} with the same source text
   */
  public PlainText plain() {
    return PlainText.of(this.sourceText);
  }

  /** Returns the format ({@link Format#TEXT} or {@link Format#HTML}) of the source text. */
  public Format getFormat() {
    return this.format;
  }

  /** Returns the source text. */
  public String getSourceText() {
    return this.sourceText;
  }

  @Override
  public boolean isEmpty() {
    return this.sourceText.isEmpty();
  }

  /**
   * Returns the source text. Translations are applied externally by a translation-aware writer, not
   * by this instance, so {@code toString()} always renders the untranslated source.
   */
  @Override
  public String toString() {
    return this.sourceText;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    return (o instanceof TranslatableText that)
        && this.sourceText.equals(that.sourceText)
        && this.format == that.format;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.sourceText, this.format);
  }

  public enum Format {
    TEXT,
    HTML
  }
}
