package com.sitepark.ies.aggregator.resolver;

import com.sitepark.ies.aggregator.value.ResolvedValue;
import java.util.List;

/**
 * Read-only view of a single CMS object, used by aggregators to navigate and resolve data.
 *
 * <p>All access is key-based. Implementations are provided by the CMS layer; aggregators never
 * access storage directly.
 */
public interface Resolver {

  /**
   * Returns the shared empty resolver, a null-object whose lookups all return defaults or empty
   * results.
   *
   * @return the empty resolver
   */
  static Resolver empty() {
    return EmptyResolver.INSTANCE;
  }

  /**
   * Returns true if the context is empty, i.e. contains no data.
   *
   * @return true if the context is empty
   */
  boolean isEmpty();

  /**
   * Returns a list of child contexts for the given key.
   *
   * @param key the field name
   * @return the child contexts, or an empty list if the key is absent
   */
  List<Resolver> resolveList(String key);

  /**
   * Returns a single child context for the given key.
   *
   * @param key the field name
   * @return the child context, or empty if the key is absent
   */
  Resolver resolve(String key);

  /**
   * Resolves the value at the given key as an instance of the specified type.
   *
   * @param key the field name
   * @return the resolved value, or empty if the key is absent or the type does not match
   */
  ResolvedValue value(String key);

  default ResolvedValue coalesce(String... keys) {
    for (String key : keys) {
      ResolvedValue value = value(key);
      if (!value.isEmpty()) {
        return value;
      }
    }
    return ResolvedValue.empty();
  }
}
