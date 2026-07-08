package com.sitepark.ies.aggregator.resolver;

import com.sitepark.ies.aggregator.value.ResolvedValue;
import java.util.List;
import java.util.Objects;

/**
 * A {@link Resolver} that is always empty — all lookups return defaults or empty results.
 *
 * <p>This class implements the <em>Null Object</em> pattern: it provides a safe, no-op
 * implementation of {@link Resolver} that can be used wherever a resolver is expected but no real
 * data source is available, eliminating the need for {@code null} checks in callers.
 *
 * <h2>Scope context</h2>
 *
 * <p>Even an empty resolver may carry a scope context. When a field is absent during navigation,
 * the resulting empty resolver still remembers the {@link #path()} of the surrounding object — and
 * therefore its {@link #root()} and {@link #globalRoot()} — so that callers can navigate back up the
 * tree from an empty result.
 *
 * <p>Use the no-argument constructor (or {@link Resolver#empty()}) when no surrounding context
 * exists; use {@link #EmptyResolver(ResolverPath)} (or {@link Resolver#empty(ResolverPath)}) when the
 * empty resolver must be embedded in an existing navigation chain.
 */
public final class EmptyResolver implements Resolver {

  static final Resolver INSTANCE = new EmptyResolver();

  private final ResolverPath path;

  /**
   * Creates an empty resolver anchored to the surrounding navigation context.
   *
   * <p>Use this constructor when a field lookup yields no result but the surrounding navigation
   * chain must remain intact. {@code path} is the path of the object in which the lookup failed and
   * is stored as-is — this resolver does <em>not</em> append itself as a new segment — so callers
   * can still navigate back up the tree via {@link #root()} / {@link #globalRoot()} even after a
   * failed lookup.
   *
   * @param path the surrounding navigation path; must not be {@code null}
   */
  EmptyResolver(ResolverPath path) {
    this.path = Objects.requireNonNull(path, "path must not be null");
  }

  /**
   * Creates a self-contained empty resolver with no surrounding context.
   *
   * <p>Both {@link #root()} and {@link #globalRoot()} return {@code this}, making this instance its
   * own scope root. This is the correct choice when no enclosing navigation chain exists — for
   * example, when used as the singleton returned by {@link Resolver#empty()}.
   */
  @SuppressWarnings("ConstructorLeaksThis") // safe: ResolverPath.of() only stores the reference
  private EmptyResolver() {
    this.path = ResolverPath.of(this);
  }

  /**
   * Returns {@code true}; this resolver is always empty.
   *
   * @return {@code true}
   */
  @Override
  public boolean isEmpty() {
    return true;
  }

  /**
   * Returns the surrounding navigation path.
   *
   * <p>For a self-contained empty resolver (created via the no-argument constructor) this is a
   * single-segment path rooted at {@code this}. For a context-aware resolver, it is the path
   * supplied at construction time.
   *
   * @return the navigation path; never {@code null}
   */
  @Override
  public ResolverPath path() {
    return this.path;
  }

  /**
   * Returns an empty list; this resolver contains no children.
   *
   * @param key the field name (ignored)
   * @return an empty, unmodifiable list
   */
  @Override
  public List<Resolver> resolveList(String key) {
    return List.of();
  }

  /**
   * Returns {@code this}; navigating an empty resolver always yields an empty resolver.
   *
   * <p>The scope context ({@link #root()} and {@link #globalRoot()}) is preserved across the call,
   * so callers can still navigate back up the tree after a failed lookup.
   *
   * @param key the field name (ignored)
   * @return {@code this}
   */
  @Override
  public Resolver resolve(String key) {
    return this;
  }

  /**
   * Returns an empty {@link ResolvedValue}; this resolver contains no values.
   *
   * @param key the field name (ignored)
   * @return an empty {@link ResolvedValue}
   */
  @Override
  public ResolvedValue value(String key) {
    return ResolvedValue.empty();
  }

  /**
   * Returns a hash code based on the identity of the scope context.
   *
   * <p>Consistent with {@link #equals(Object)}: two instances anchored to the same {@code root} and
   * {@code globalRoot} references produce the same hash code.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        System.identityHashCode(this.path.root()), System.identityHashCode(this.path.globalRoot()));
  }

  /**
   * Compares this resolver to another object for equality.
   *
   * <p>Two {@code EmptyResolver} instances are considered equal if and only if they are anchored to
   * the same {@code root} and {@code globalRoot} references (identity comparison via {@code ==}).
   * Reference equality is used deliberately: resolvers represent live navigation contexts, and two
   * empty resolvers anchored to different root instances are not interchangeable even if those roots
   * happen to be structurally identical. The intermediate segments of the path do not participate in
   * equality.
   *
   * @param o the object to compare with
   * @return {@code true} if {@code o} is an {@code EmptyResolver} anchored to the same {@code root}
   *     and {@code globalRoot} references as this instance
   */
  @Override
  public boolean equals(Object o) {
    return (o instanceof EmptyResolver that)
        && this.path.root() == that.path.root()
        && this.path.globalRoot() == that.path.globalRoot();
  }
}
