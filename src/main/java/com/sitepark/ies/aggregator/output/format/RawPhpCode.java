package com.sitepark.ies.aggregator.output.format;

import java.util.Objects;

/**
 * Typed marker for values that should be emitted as raw PHP code instead of being serialized.
 *
 * <p>Used as a value placed into an {@code OutputObject} when the {@code PhpArrayWriter} should
 * emit the contained string verbatim — without quoting or escaping — for example, a PHP function
 * call or a constant reference. Other writers (JSON, Map) treat this as an opaque value and fall
 * back to {@code toString()}.
 */
public final class RawPhpCode {

  private final String code;

  /**
   * @param code the raw PHP code string; must not be {@code null}
   */
  public RawPhpCode(String code) {
    this.code = Objects.requireNonNull(code, "code must not be null");
  }

  /** Returns the raw PHP code string. */
  public String code() {
    return this.code;
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
