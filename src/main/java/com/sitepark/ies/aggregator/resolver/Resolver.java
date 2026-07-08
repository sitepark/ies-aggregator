package com.sitepark.ies.aggregator.resolver;

import com.sitepark.ies.aggregator.value.ResolvedValue;
import java.util.List;

/**
 * Read-only view of a single CMS object, used by aggregators to navigate and resolve data.
 *
 * <p>All access is key-based. Implementations are provided by the CMS layer; aggregators never
 * access storage directly.
 *
 * <h2>Navigation model</h2>
 *
 * <p>A resolver represents a node in a tree of CMS data. Callers navigate the tree by chaining
 * {@link #resolve(String)} calls:
 *
 * <pre>{@code
 * resolver.resolve("address").resolve("city").value("name");
 * }</pre>
 *
 * <h2>Links and scope boundaries</h2>
 *
 * <p>A field may reference another CMS object via a <em>link</em>. Resolving such a field crosses a
 * scope boundary: the returned resolver represents the root of the linked object, and subsequent
 * navigation is relative to that object.
 *
 * <pre>{@code
 * // "author" is a link to a separate CMS object
 * resolver.resolve("author").resolve("name").value("text");
 * //               ↑ crosses scope boundary
 * }</pre>
 *
 * <p>Use {@link #root()} to return to the top of the current scope and {@link #globalRoot()} to
 * return to the top of the outermost scope. The complete step-by-step path from the global root to
 * this resolver is available via {@link #path()}.
 *
 * <h2>Notes for implementations</h2>
 *
 * <p>Implementations should keep the default (identity-based) {@link Object#equals(Object)} and
 * {@link Object#hashCode()} and must not include {@link #path()} in their {@link Object#toString()}:
 * a resolver's own path ends with a segment pointing back at that resolver, so content-based
 * equality or a path-printing {@code toString} could recurse indefinitely.
 */
public interface Resolver {

  /**
   * Returns an empty resolver anchored to the given navigation path.
   *
   * <p>Use this when a lookup yields no result but the surrounding navigation chain must remain
   * intact: {@code path} is the path of the object in which the lookup failed, so callers can still
   * navigate back up the tree via {@link #root()} / {@link #globalRoot()}.
   *
   * @param path the surrounding navigation path; must not be {@code null}
   * @return an empty resolver carrying the given path
   */
  static Resolver empty(ResolverPath path) {
    return new EmptyResolver(path);
  }

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
   * Returns {@code true} if this resolver contains no data.
   *
   * <p>An empty resolver is returned whenever a key is absent or a link cannot be followed. Calling
   * any navigation or value method on an empty resolver is safe and returns an appropriate empty
   * result.
   *
   * @return {@code true} if this resolver contains no data
   */
  boolean isEmpty();

  /**
   * Returns the full navigation path from the outermost root resolver to this resolver.
   *
   * <p>Unlike {@link #root()} and {@link #globalRoot()}, which only expose the current and outermost
   * scope roots, the path records every step taken to reach this resolver and never resets — it
   * grows across every link/scope boundary crossed. Implementations extend their parent's path by
   * one segment when they are created — see {@link ResolverPath#descend(String, ResolverPath.Factory)}
   * and {@link ResolverPath#enterScope(String, ResolverPath.Factory)}.
   *
   * @return this resolver's path; never {@code null}
   */
  ResolverPath path();

  /**
   * Returns the root resolver of the current object scope.
   *
   * <p>Each time a link is followed via {@link #resolve(String)}, a new scope begins. This method
   * navigates back to the top of that scope — the root of the most recently entered linked object —
   * rather than the global root of the entire resolver tree.
   *
   * <pre>{@code
   * // Without a link: returns the top of the original object
   * resolver.resolve("a").resolve("b").root()
   *     == resolver
   *
   * // With a link: returns the top of the linked object, not the original root
   * resolver.resolve("author")   // crosses link boundary into Author object
   *          .resolve("address") // navigates inside Author
   *          .root()             // → root of Author, not the original resolver
   *          .resolve("name")    // → Author.name
   * }</pre>
   *
   * <p>The default implementation delegates to {@link #path()}.
   *
   * @return the root resolver of the current scope; never {@code null}
   * @see #globalRoot()
   */
  default Resolver root() {
    return path().root();
  }

  /**
   * Returns the root resolver of the outermost scope, regardless of how many link boundaries have
   * been crossed.
   *
   * <p>While {@link #root()} is relative to the most recently entered linked object, {@code
   * globalRoot()} always returns the resolver at the very top of the entire navigation chain — the
   * object with which the original call sequence started.
   *
   * <pre>{@code
   * // Navigating through two linked objects and then back to the very beginning:
   * resolver.resolve("author")   // crosses into Author object
   *          .resolve("company") // crosses into Company object
   *          .globalRoot()       // → the original resolver, not Author or Company
   *          .resolve("title")   // → field of the original object
   * }</pre>
   *
   * <p>The default implementation delegates to {@link #path()}.
   *
   * @return the root resolver of the outermost scope; never {@code null}
   * @see #root()
   */
  default Resolver globalRoot() {
    return path().globalRoot();
  }

  /**
   * Returns a list of child resolvers for the given key.
   *
   * <p>Use this method for fields that contain multiple values or sub-objects, such as lists or
   * repeated sections.
   *
   * @param key the field name; must not be {@code null}
   * @return an unmodifiable list of child resolvers; empty if the key is absent or holds no entries
   */
  List<Resolver> resolveList(String key);

  /**
   * Returns a single child resolver for the given key.
   *
   * <p>If the key is absent or the field holds no value, an empty resolver is returned. The
   * returned resolver is never {@code null}; callers do not need to perform a null-check.
   *
   * @param key the field name; must not be {@code null}
   * @return the child resolver for the given key, or an empty resolver if the key is absent
   * @see #isEmpty()
   */
  Resolver resolve(String key);

  /**
   * Returns the value at the given key as a {@link ResolvedValue}.
   *
   * <p>If the key is absent, or the stored data cannot be represented as a value (e.g. it is a
   * nested object), an empty {@link ResolvedValue} is returned. The result is never {@code null}.
   *
   * @param key the field name; must not be {@code null}
   * @return the resolved value, or an empty {@link ResolvedValue} if the key is absent
   */
  ResolvedValue value(String key);

  /**
   * Returns the first non-empty value found among the given keys.
   *
   * <p>Keys are evaluated in the order they are provided. As soon as a key yields a non-empty
   * {@link ResolvedValue}, that value is returned and the remaining keys are not evaluated. If no
   * key yields a value, an empty {@link ResolvedValue} is returned.
   *
   * <pre>{@code
   * // Use "displayName" if present, fall back to "name", then to "shortName"
   * ResolvedValue name = resolver.coalesce("displayName", "name", "shortName");
   * }</pre>
   *
   * @param keys the field names to try, in order; must not be {@code null}
   * @return the first non-empty resolved value, or an empty {@link ResolvedValue} if none found
   */
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
