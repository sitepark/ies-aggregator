package com.sitepark.ies.aggregator.value;

/**
 * An immutable, identity-based source text that can be translated.
 *
 * <p>Unlike {@link PlainText}, this type is used <em>by identity</em>: it is collected from the output
 * tree by a {@code TranslatableTextCollector} and serves as the key of an external translation
 * table (see {@code com.sitepark.ies.aggregator.output.collect.Translations}). The instance itself
 * only carries the source text and its {@link Format}; the translation lives outside the tree.
 *
 * <p>Because instances are used as identity keys, this type intentionally does <strong>not</strong>
 * define {@code equals}/{@code hashCode}: two distinct occurrences of the same source text may be
 * translated differently depending on their position in the tree.
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
   * Because this type is used by identity as a translation-table key, a copy is an independent
   * translation slot: it can be translated separately from the instance it was copied from.
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

  /**
   * Returns the source text. Translations are applied externally by a translation-aware writer, not
   * by this instance, so {@code toString()} always renders the untranslated source.
   */
  @Override
  public String toString() {
    return this.sourceText;
  }

  public enum Format {
    TEXT,
    HTML
  }
}
