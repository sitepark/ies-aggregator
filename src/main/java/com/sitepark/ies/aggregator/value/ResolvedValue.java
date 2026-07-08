package com.sitepark.ies.aggregator.value;

import com.sitepark.ies.aggregator.value.text.PlainText;
import com.sitepark.ies.aggregator.value.text.TranslatableText;
import com.sitepark.ies.aggregator.value.uri.PlainUri;
import com.sitepark.ies.aggregator.value.uri.Uri;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * Wraps a nullable value resolved from a {@link com.sitepark.ies.aggregator.resolver.Resolver
 * Resolver}, providing typed accessor methods.
 *
 * <p>An empty resolved value ({@link #EMPTY}) indicates that no value was found for the requested
 * key. Calling a typed accessor on an empty value without a default throws {@link
 * IllegalArgumentException}.
 */
public final class ResolvedValue implements Emptiable {
  private static final String VALUE_NOT_SET = "Value not set";

  private final @Nullable Object value;

  /** Singleton empty value — indicates that no value is present. */
  private static final ResolvedValue EMPTY = new ResolvedValue(null);

  /**
   * @param value the wrapped value, or {@code null} to indicate absence
   */
  private ResolvedValue(@Nullable Object value) {
    this.value = value;
  }

  public static ResolvedValue empty() {
    return EMPTY;
  }

  public static ResolvedValue of(Object value) {
    if (value instanceof Collection) {
      if (!(value instanceof List<?> list)) {
        throw new IllegalArgumentException(
            "Only Collection-Type " + List.class.getName() + " values are supported");
      }
      if (list.isEmpty()) {
        value = null;
      }
    }
    if (value instanceof Object[]) {
      throw new IllegalArgumentException("Arrays are not supported, use a List instead");
    }
    return new ResolvedValue(value);
  }

  /** Returns {@code true} if no value is present. */
  @Override
  public boolean isEmpty() {
    return value == null;
  }

  public boolean isList() {
    return value instanceof List;
  }

  public <T> List<T> asList(Class<T> type) {
    if (this.value == null || !(this.value instanceof List<?> list)) {
      throw new IllegalArgumentException("Value is not a list");
    }
    return list.stream().map(type::cast).toList();
  }

  /** Returns the raw wrapped value, or {@code null} if empty. */
  public @Nullable Object value() {
    return this.value;
  }

  /**
   * Returns the single scalar value, unwrapping a one-element list.
   *
   * <p>A non-list value is returned as-is. A list is only accepted when it holds exactly one
   * element; a list with more than one element is rejected rather than silently using the first
   * element, which would be a footgun for callers expecting a scalar.
   *
   * @throws IllegalArgumentException if the value is a list with more than one element
   */
  private Object singleItemIfList() {
    Object current = Objects.requireNonNull(this.value);
    if (!(current instanceof List<?> list)) {
      return current;
    }
    if (list.size() > 1) {
      throw new IllegalArgumentException(
          "Value is a list with " + list.size() + " elements; expected a single value");
    }
    return list.getFirst();
  }

  private static boolean isIntegral(Number value) {
    return value instanceof Integer
        || value instanceof Long
        || value instanceof Short
        || value instanceof Byte
        || value instanceof BigInteger;
  }

  /**
   * Returns the value as an {@code int}.
   *
   * @throws IllegalArgumentException if empty or the value is not an integral number
   */
  public int asInt() {
    return asInt(0);
  }

  /**
   * Returns the value as an {@code int}, or {@code defaultValue} if empty.
   *
   * <p>Accepts any integral {@link Number} ({@link Integer}, {@link Long}, {@link Short}, {@link
   * Byte}, {@link BigInteger}); fractional types are rejected to avoid silent truncation.
   *
   * @param defaultValue the value to return when empty
   * @throws IllegalArgumentException if not empty and the value is not an integral number
   */
  public int asInt(int defaultValue) {
    if (this.isEmpty()) {
      return defaultValue;
    }
    Object value = singleItemIfList();
    if (value instanceof Number number && isIntegral(number)) {
      return number.intValue();
    }
    throw new IllegalArgumentException(
        "Value is not an integral number (" + value.getClass().getName() + ")");
  }

  /**
   * Returns the value as a {@code long}.
   *
   * @throws IllegalArgumentException if empty or the value is not an integral number
   */
  public long asLong() {
    return asLong(0);
  }

  /**
   * Returns the value as a {@code long}, or {@code defaultValue} if empty.
   *
   * <p>Accepts any integral {@link Number} ({@link Integer}, {@link Long}, {@link Short}, {@link
   * Byte}, {@link BigInteger}); fractional types are rejected to avoid silent truncation.
   *
   * @param defaultValue the value to return when empty
   * @throws IllegalArgumentException if not empty and the value is not an integral number
   */
  public long asLong(long defaultValue) {
    if (this.isEmpty()) {
      return defaultValue;
    }
    Object value = singleItemIfList();
    if (value instanceof Number number && isIntegral(number)) {
      return number.longValue();
    }
    throw new IllegalArgumentException(
        "Value is not an integral number (" + value.getClass().getName() + ")");
  }

  /**
   * Returns the value as a {@code float}.
   *
   * @throws IllegalArgumentException if empty or the value is not a {@link Number}
   */
  public float asFloat() {
    return asFloat(0.0f);
  }

  /**
   * Returns the value as a {@code float}, or {@code defaultValue} if empty.
   *
   * <p>Accepts any {@link Number} and narrows it via {@link Number#floatValue()}.
   *
   * @param defaultValue the value to return when empty
   * @throws IllegalArgumentException if not empty and the value is not a {@link Number}
   */
  public float asFloat(float defaultValue) {
    if (this.isEmpty()) {
      return defaultValue;
    }
    Object value = singleItemIfList();
    if (value instanceof Number number) {
      return number.floatValue();
    }
    throw new IllegalArgumentException(
        "Value is not a number (" + value.getClass().getName() + ")");
  }

  /**
   * Returns the value as a {@code double}.
   *
   * @throws IllegalArgumentException if empty or the value is not a {@link Number}
   */
  public double asDouble() {
    return asDouble(0.0);
  }

  /**
   * Returns the value as a {@code double}, or {@code defaultValue} if empty.
   *
   * <p>Accepts any {@link Number} and widens it via {@link Number#doubleValue()}.
   *
   * @param defaultValue the value to return when empty
   * @throws IllegalArgumentException if not empty and the value is not a {@link Number}
   */
  public double asDouble(double defaultValue) {
    if (this.isEmpty()) {
      return defaultValue;
    }
    Object value = singleItemIfList();
    if (value instanceof Number number) {
      return number.doubleValue();
    }
    throw new IllegalArgumentException(
        "Value is not a number (" + value.getClass().getName() + ")");
  }

  /**
   * Returns the value as a string via {@code toString()}.
   *
   * @throws IllegalArgumentException if empty
   */
  public String asString() {
    return asString("");
  }

  /**
   * Returns the value as a string via {@code toString()}, or {@code defaultValue} if empty.
   *
   * @param defaultValue the value to return when empty
   */
  public String asString(String defaultValue) {
    if (this.isEmpty()) {
      return defaultValue;
    }
    Object value = singleItemIfList();
    return value.toString();
  }

  /**
   * Returns the value as a {@link PlainText}.
   *
   * @throws IllegalArgumentException if empty or the value is not a {@link PlainText} or {@link
   *     String}
   */
  public PlainText asText() {
    return asText(PlainText.EMPTY);
  }

  /**
   * Returns the value as a {@link PlainText}, or {@code defaultValue} if empty.
   *
   * @param defaultValue the value to return when empty
   * @throws IllegalArgumentException if not empty and the value is not a {@link PlainText} or
   *     {@link String}
   */
  public PlainText asText(PlainText defaultValue) {
    if (this.isEmpty()) {
      return defaultValue;
    }
    Object value = singleItemIfList();
    return switch (value) {
      case PlainText text -> text;
      case TranslatableText translatableText -> translatableText.plain();
      case String stringValue -> PlainText.of(stringValue);
      case null, default ->
          throw new IllegalArgumentException(
              "Value is not an string or Text ( " + value.getClass().getName() + ")");
    };
  }

  /**
   * Returns the value as a {@link PlainUri}.
   *
   * @throws IllegalArgumentException if empty or the value is not a {@link PlainUri} or {@link
   *     String}
   */
  public PlainUri asUri() {
    return asUri(Uri.empty());
  }

  /**
   * Returns the value as a {@link PlainUri}, or {@code defaultValue} if empty.
   *
   * @param defaultValue the value to return when empty
   * @throws IllegalArgumentException if not empty and the value is not a {@link PlainUri} or {@link
   *     String}
   */
  public PlainUri asUri(PlainUri defaultValue) {
    if (this.isEmpty()) {
      return defaultValue;
    }
    String s = asString();
    if (s.isBlank()) {
      return Uri.empty();
    }

    return PlainUri.of(s);
  }

  /**
   * Returns the value as an enum constant of {@code enumClass}.
   *
   * @param <T> the enum type
   * @param enumClass the enum class
   * @throws IllegalArgumentException if empty, or the value cannot be converted to the enum
   */
  public <T extends Enum<T>> T asEnum(Class<T> enumClass) {

    if (this.isEmpty()) {
      throw new IllegalArgumentException(VALUE_NOT_SET);
    }
    return Objects.requireNonNull(asEnum(enumClass, null));
  }

  /**
   * Returns the value as an enum constant of {@code enumClass}, or {@code defaultValue} if empty.
   *
   * @param <T> the enum type
   * @param enumClass the enum class
   * @param defaultValue the value to return when empty
   * @throws IllegalArgumentException if not empty and the value cannot be converted to the enum
   */
  public <T extends Enum<T>> @Nullable T asEnum(Class<T> enumClass, @Nullable T defaultValue) {

    if (this.isEmpty()) {
      return defaultValue;
    }

    Object value = singleItemIfList();
    if (enumClass.isInstance(value)) {
      return enumClass.cast(value);
    }

    if (!(value instanceof String stringValue)) {
      throw new IllegalArgumentException(
          "Value is not an string to create enum "
              + enumClass
              + "("
              + value.getClass().getName()
              + ")");
    }

    for (T e : enumClass.getEnumConstants()) {
      if (e.name().equals(stringValue)) {
        return e;
      }
      if (e instanceof NamedEnum namedEnum && namedEnum.getName().equals(stringValue)) {
        return e;
      }
    }

    throw new IllegalArgumentException(
        "Value is not an enum " + enumClass.getName() + " (" + value + ")");
  }

  /**
   * Returns the value as a {@code boolean}.
   *
   * @throws IllegalArgumentException if empty or the value is not a {@link Boolean} or {@link
   *     String}
   */
  public boolean asBoolean() {
    return asBoolean(false);
  }

  /**
   * Returns the value as a {@code boolean}, or {@code defaultValue} if empty.
   *
   * @param defaultValue the value to return when empty
   * @throws IllegalArgumentException if not empty and the value is not a {@link Boolean} or {@link
   *     String}
   */
  public boolean asBoolean(boolean defaultValue) {
    if (this.isEmpty()) {
      return defaultValue;
    }
    Object value = singleItemIfList();
    if (value instanceof Boolean booleanValue) {
      return booleanValue;
    }
    if (value instanceof String stringValue) {
      return Boolean.parseBoolean(stringValue);
    }
    throw new IllegalArgumentException(
        "Value is not an boolean (" + value.getClass().getName() + ")");
  }

  /**
   * Returns the value as {@code type}.
   *
   * <p>If the value is already an instance of {@code type}, it is returned as-is. If it is a {@link
   * String}, it is deserialized via {@code parser} (typically parsing embedded JSON). Otherwise an
   * exception is thrown.
   *
   * @param <T> the target type
   * @param type the target type
   * @param defaultValue the value to return when empty
   * @param parser the parser used to deserialize a string value
   * @throws IllegalArgumentException if empty, or the value can neither be cast nor parsed
   */
  public <T> T as(Class<T> type, StructuredValueParser parser, T defaultValue) {
    if (this.isEmpty()) {
      return defaultValue;
    }
    Object value = singleItemIfList();
    if (type.isInstance(value)) {
      return type.cast(value);
    }
    if (value instanceof String raw) {
      return parser.parse(raw, type);
    }
    throw new IllegalArgumentException(
        "Value cannot be converted to " + type.getName() + " (" + value.getClass().getName() + ")");
  }

  /**
   * Returns the value as a {@link Map}, deserializing a string value via {@code parser} (typically
   * parsing embedded JSON) when needed.
   *
   * @param parser the parser used to deserialize a string value
   * @throws IllegalArgumentException if empty, or the value is neither a {@link Map} nor a {@link
   *     String}
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> asMap(StructuredValueParser parser) {
    return as(Map.class, parser, Map.of());
  }

  public ResolvedValue orElse(Supplier<ResolvedValue> other) {
    return this.isEmpty() ? other.get() : this;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    return (o instanceof ResolvedValue that) && Objects.equals(this.value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.value);
  }
}
