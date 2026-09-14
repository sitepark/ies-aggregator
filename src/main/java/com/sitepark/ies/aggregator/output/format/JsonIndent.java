package com.sitepark.ies.aggregator.output.format;

/**
 * How a {@link JsonWriter} indents the document it writes.
 *
 * <p>Indentation is a product decision, not a property of the JSON format: the same tree is a
 * one-line payload in an HTTP response and a hand-editable block inside a generated file. A writer
 * therefore takes this record instead of guessing, and {@link #NONE} — compact output — is what it
 * uses when nobody says otherwise.
 *
 * <p>{@code factor} is the number of {@code indentChar}s added per nesting level. {@code initial} is
 * the column the document starts at: the opening brace is written where the caller already stands,
 * every line below it — the closing brace included — is indented by at least {@code initial}. That
 * is what embedding a document into an already indented file needs.
 *
 * <p>A {@code factor} of zero means compact: no line breaks and no indentation at all, so {@code
 * initial} has nothing to apply to and is ignored.
 *
 * @param factor the number of indent characters per nesting level, zero for compact output
 * @param initial the number of indent characters every line of the document starts with
 * @param indentChar the character to indent with, typically a space or a tab
 */
public record JsonIndent(int factor, int initial, char indentChar) {

  /** Compact output: everything on one line, no indentation. */
  public static final JsonIndent NONE = new JsonIndent(0, 0, ' ');

  /**
   * Creates an indentation.
   *
   * @throws IllegalArgumentException if {@code factor} or {@code initial} is negative
   */
  public JsonIndent {
    if (factor < 0) {
      throw new IllegalArgumentException("factor must not be negative: " + factor);
    }
    if (initial < 0) {
      throw new IllegalArgumentException("initial must not be negative: " + initial);
    }
  }

  /**
   * Indents with {@code factor} spaces per nesting level, starting at column zero.
   *
   * @param factor the number of spaces per nesting level, zero for compact output
   * @return the indentation
   * @throws IllegalArgumentException if {@code factor} is negative
   */
  public static JsonIndent of(int factor) {
    return new JsonIndent(factor, 0, ' ');
  }

  /**
   * Whether the document is written on a single line.
   *
   * @return {@code true} if nothing is indented and no line breaks are written
   */
  public boolean isCompact() {
    return this.factor == 0;
  }
}
