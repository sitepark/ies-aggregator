package com.sitepark.ies.aggregator.output.format;

import java.util.Objects;

/**
 * Typed marker for values that should be emitted as raw PHP code instead of being serialized.
 *
 * <p>Used as a value placed into an {@code OutputObject} when the {@code PhpArrayWriter} should
 * emit the contained string verbatim — without quoting or escaping — for example, a PHP function
 * call or a constant reference. Other writers (JSON, Map) treat this as an opaque value and fall
 * back to {@code toString()}.
 *
 * <p>Raw code is never {@link #isEmpty() empty}, so it is always rendered — even when the code
 * string itself is empty. Declare a field as {@link Code} to let a single field carry either raw PHP
 * code or {@link PlainCode plain content}.
 */
public final class RawPhpCode implements Code {

  private final String code;

  /**
   * @param code the raw PHP code string; must not be {@code null}
   */
  public RawPhpCode(String code) {
    this.code = Objects.requireNonNull(code, "code must not be null");
  }

  /**
   * Creates a {@code RawPhpCode} from the given code string.
   *
   * @param code the raw PHP code string; must not be {@code null}
   * @return a new {@code RawPhpCode} instance
   * @throws NullPointerException if {@code code} is {@code null}
   */
  public static RawPhpCode of(String code) {
    return new RawPhpCode(code);
  }

  /** Returns the raw PHP code string. */
  @Override
  public String code() {
    return this.code;
  }

  /** Always returns {@code false} — raw code is never treated as empty. */
  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public String toString() {
    return this.code;
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof RawPhpCode that) && this.code.equals(that.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.code);
  }
}
