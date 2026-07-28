package com.sitepark.ies.aggregator.output.format;

import java.util.Objects;

/**
 * A {@link Code} that carries plain content instead of executable code.
 *
 * <p>Unlike {@link RawPhpCode}, instances of this type are not emitted verbatim: every writer quotes
 * and escapes the content like any other string. Use it for the values of a {@code Code} field that
 * happen to be plain content rather than code.
 *
 * <p>The content is stored verbatim — it is neither trimmed nor otherwise normalized. An instance
 * whose content is empty ({@code ""}) reports {@link #isEmpty() empty} and is therefore dropped from
 * the output like any other empty value.
 */
public final class PlainCode implements Code {

  /** The empty content ({@code ""}). */
  public static final PlainCode EMPTY = new PlainCode("");

  private final String content;

  private PlainCode(String content) {
    this.content = Objects.requireNonNull(content, "content must not be null");
  }

  /**
   * Creates a {@code PlainCode} from the given content.
   *
   * @param content the content; must not be {@code null}
   * @return a new {@code PlainCode} instance
   * @throws NullPointerException if {@code content} is {@code null}
   */
  public static PlainCode of(String content) {
    return new PlainCode(content);
  }

  /** Returns the content string. */
  @Override
  public String code() {
    return this.content;
  }

  @Override
  public boolean isEmpty() {
    return this.content.isEmpty();
  }

  @Override
  public String toString() {
    return this.content;
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof PlainCode that) && this.content.equals(that.content);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.content);
  }
}
