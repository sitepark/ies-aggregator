package com.sitepark.ies.aggregator.value;

/**
 * Deserializes a raw (e.g. JSON) string into a structured value.
 *
 * <p>The aggregator module ships no JSON deserializer of its own. Concrete implementations (e.g.
 * backed by Jackson) live as adapters outside this module and are passed to the typed {@code
 * ResolvedValue} accessors that need them ({@link ResolvedValue#as(Class, StructuredValueParser,
 * Object)}, {@link ResolvedValue#asMap(StructuredValueParser)}).
 */
@FunctionalInterface
public interface StructuredValueParser {

  /**
   * Parses {@code raw} into an instance of {@code type}.
   *
   * @param raw the raw string payload (typically JSON)
   * @param type the target type
   * @param <T> the target type
   * @return the deserialized value
   */
  <T> T parse(String raw, Class<T> type);
}
