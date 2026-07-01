package com.sitepark.ies.aggregator.value.text;

/**
 * A text value an aggregator writes into the output tree.
 *
 * <p>A {@code Text} is one of two kinds:
 *
 * <ul>
 *   <li>{@link PlainText} — rendered verbatim and never translated.
 *   <li>{@link TranslatableText} — collected into the translation table and rendered through it.
 * </ul>
 *
 * <p>Because the {@link com.sitepark.ies.aggregator.output.OutputVisitor OutputVisitor} dispatches
 * on the runtime type, a field declared as {@code Text} lets the aggregator decide per value
 * whether a text should be translatable — both kinds are written into the same field.
 */
public sealed interface Text permits PlainText, TranslatableText {

  /** Represents the empty text ({@code ""}). */
  PlainText EMPTY = new PlainText("");

  /**
   * Creates a non-translatable, verbatim {@link PlainText}.
   *
   * <p>The text will be trimmed of leading and trailing whitespace.
   *
   * @param text the text content; must not be {@code null}
   * @return a new {@link PlainText} instance
   * @throws NullPointerException if {@code text} is {@code null}
   */
  static PlainText of(String text) {
    return PlainText.of(text);
  }

  static PlainText empty() {
    return EMPTY;
  }
}
