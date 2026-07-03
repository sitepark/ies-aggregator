package com.sitepark.ies.aggregator.value;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * A value that carries a notion of emptiness.
 *
 * <p>Implemented by all value types an aggregator writes into the output tree that can be empty,
 * such as {@link com.sitepark.ies.aggregator.value.text.Text Text}, {@link
 * com.sitepark.ies.aggregator.value.uri.Uri Uri} and {@link ResolvedValue}. It lets the output tree
 * treat emptiness uniformly &mdash; see {@link
 * com.sitepark.ies.aggregator.output.OutputNode#putIfNotEmpty(String, Object)}.
 */
@FunctionalInterface
public interface Emptiable {

  /** Returns {@code true} if this value is empty. */
  boolean isEmpty();

  /**
   * Returns {@code true} if {@code value} is considered empty.
   *
   * <p>A value is empty when it is {@code null}, an {@link Emptiable} that {@link #isEmpty() reports
   * empty}, an empty {@link CharSequence}, an empty {@link Collection}, an empty {@link Map}, or an
   * array of length {@code 0}. Any other value is treated as non-empty.
   *
   * @param value the value to inspect; may be {@code null}
   * @return {@code true} if the value is empty
   */
  static boolean isEmpty(@Nullable Object value) {
    return switch (value) {
      case null -> true;
      case Emptiable emptiable -> emptiable.isEmpty();
      case CharSequence charSequence -> charSequence.isEmpty();
      case Collection<?> collection -> collection.isEmpty();
      case Map<?, ?> map -> map.isEmpty();
      default -> value.getClass().isArray() && Array.getLength(value) == 0;
    };
  }
}
