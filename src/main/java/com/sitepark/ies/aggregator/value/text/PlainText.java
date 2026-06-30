package com.sitepark.ies.aggregator.value.text;

import java.util.Objects;

/**
 * A non-translatable {@link Text}: a plain string rendered verbatim.
 *
 * <p>Unlike {@link TranslatableText}, instances of this type are never collected into the
 * translation table; a translation-aware writer renders them as-is. Can be promoted to a {@link
 * TranslatableText} via {@link #translatable()}.
 */
public final class PlainText implements Text {
  private final String text;

  /** Represents the empty text ({@code ""}). */
  public static final PlainText EMPTY = new PlainText("");

  /**
   * Creates a {@code PlainText} from the given content.
   *
   * <p>Leading and trailing whitespace is stripped via {@link String#trim()}; the stored value is
   * the trimmed string. Two instances whose content differs only in surrounding whitespace are
   * therefore equal.
   *
   * @param text the text content; must not be {@code null}
   */
  private PlainText(String text) {
    Objects.requireNonNull(text, "Text must not be null");
    this.text = text.trim();
  }

  /**
   * Creates a new {@code PlainText} instance from the given string.
   *
   * <p>The text will be trimmed of leading and trailing whitespace.
   *
   * @param text the text content; must not be {@code null}
   * @return a new {@code PlainText} instance
   * @throws NullPointerException if {@code text} is {@code null}
   */
  public static PlainText of(String text) {
    return new PlainText(text);
  }

  /** Returns a new {@link TranslatableText} with {@link TranslatableText.Format#TEXT} format. */
  public TranslatableText translatable() {
    return TranslatableText.of(this.text);
  }

  /**
   * Returns a new {@link TranslatableText} with the given format.
   *
   * @param format the text format
   */
  public TranslatableText translatable(TranslatableText.Format format) {
    return TranslatableText.of(this.text, format);
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof PlainText that) && this.text.equals(that.text);
  }

  @Override
  public int hashCode() {
    return this.text.hashCode();
  }

  @Override
  public String toString() {
    return this.text;
  }
}
