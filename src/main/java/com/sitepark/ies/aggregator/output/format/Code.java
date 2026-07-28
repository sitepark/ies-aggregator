package com.sitepark.ies.aggregator.output.format;

import com.sitepark.ies.aggregator.value.Emptiable;

/**
 * A code value an aggregator writes into the output tree: either raw code or plain content.
 *
 * <p>A {@code Code} is one of two kinds:
 *
 * <ul>
 *   <li>{@link RawPhpCode} — emitted verbatim by the {@link PhpArrayWriter}, without quoting or
 *       escaping, e.g. a PHP function call or a constant reference.
 *   <li>{@link PlainCode} — ordinary content, quoted and escaped like any other string.
 * </ul>
 *
 * <p>Because the {@link com.sitepark.ies.aggregator.output.OutputVisitor OutputVisitor} dispatches on
 * the runtime type, a model may declare a <b>single</b> field of type {@code Code} and decide per
 * value whether it carries executable code or plain content:
 *
 * <pre>{@code
 * public record Callback(Code handler) {}
 *
 * Callback.of(Code.php("myHandler()"));  // "handler" => myHandler()
 * Callback.of(Code.of("myHandler"));     // "handler" => "myHandler"
 * }</pre>
 */
public sealed interface Code extends Emptiable permits PlainCode, RawPhpCode {

  /**
   * Creates a {@link PlainCode} carrying plain content, which every writer quotes and escapes like
   * any other string.
   *
   * @param content the content; must not be {@code null}
   * @return a new {@link PlainCode} instance
   * @throws NullPointerException if {@code content} is {@code null}
   */
  static PlainCode of(String content) {
    return PlainCode.of(content);
  }

  /**
   * Creates a {@link RawPhpCode} that the {@link PhpArrayWriter} emits verbatim.
   *
   * @param code the raw PHP code string; must not be {@code null}
   * @return a new {@link RawPhpCode} instance
   * @throws NullPointerException if {@code code} is {@code null}
   */
  static RawPhpCode php(String code) {
    return RawPhpCode.of(code);
  }

  /** Returns the empty plain content ({@code ""}). */
  static PlainCode empty() {
    return PlainCode.EMPTY;
  }

  /**
   * Returns the string this value carries — the raw code for a {@link RawPhpCode}, the content for a
   * {@link PlainCode}.
   */
  String code();
}
