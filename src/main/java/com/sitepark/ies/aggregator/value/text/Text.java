package com.sitepark.ies.aggregator.value.text;

import com.sitepark.ies.aggregator.value.Emptiable;
import java.util.function.Supplier;

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
public sealed interface Text extends Emptiable permits PlainText, TranslatableText {

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
    return PlainText.EMPTY;
  }

  PlainText toPlainText();

  Text translatable();

  /**
   * Returns this text when it is non-empty, otherwise {@code other}.
   *
   * @param other the replacement text used when this text is empty; must not be {@code null}
   * @return this text when non-empty, otherwise {@code other}
   */
  default Text orElse(Text other) {
    return this.isEmpty() ? other : this;
  }

  /**
   * Returns this text when it is non-empty, otherwise the text produced by {@code fallback}.
   *
   * <p>The fallback is only evaluated when this text is empty, so an expensive fallback (e.g.
   * assembling a headline from a linked object) is skipped whenever a value is already present.
   *
   * @param fallback supplies the replacement text when this text is empty; must not be {@code null}
   * @return this text when non-empty, otherwise {@code fallback.get()}
   */
  default Text orElse(Supplier<? extends Text> fallback) {
    return this.isEmpty() ? fallback.get() : this;
  }
}
