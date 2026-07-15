package com.sitepark.ies.aggregator.resolver;

import com.sitepark.ies.aggregator.value.ResolvedValue;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * An {@link EntityResolver} that is always empty — the entity counterpart of {@link EmptyResolver}.
 *
 * <p>This class implements the <em>Null Object</em> pattern for links: it is returned by {@link
 * Resolver#resolveLink(String)} when a link field is absent or cannot be followed, so callers can
 * stay on the {@link EntityResolver} type without casting or null-checking. All lookups return
 * defaults or empty results and all entity attributes are neutral ({@link #entityId()} is {@code
 * 0}, {@link #entityType()}/{@link #entityName()} are empty, {@link #parentGroup()} is {@code
 * null}).
 *
 * <p>Like {@link EmptyResolver}, it carries the {@link #path()} of the object in which the link
 * lookup failed, so callers can still navigate back up the tree via {@link #root()} / {@link
 * #globalRoot()}.
 */
public final class EmptyEntityResolver implements EntityResolver {

  private final ResolverPath path;

  /**
   * Creates an empty entity resolver anchored to the surrounding navigation context.
   *
   * @param path the surrounding navigation path; must not be {@code null}
   */
  EmptyEntityResolver(ResolverPath path) {
    this.path = Objects.requireNonNull(path, "path must not be null");
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
   * Returns the surrounding navigation path supplied at construction time.
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
   * Returns {@code this}; navigating an empty entity resolver always yields an empty resolver.
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
   * Returns {@code 0}; an empty entity has no id.
   *
   * @return {@code 0}
   */
  @Override
  public int entityId() {
    return 0;
  }

  /**
   * Returns the empty string; an empty entity has no type.
   *
   * @return the empty string
   */
  @Override
  public String entityType() {
    return "";
  }

  /**
   * Returns the empty string; an empty entity has no name.
   *
   * @return the empty string
   */
  @Override
  public String entityName() {
    return "";
  }

  /**
   * Returns {@code null}; an empty entity has no parent group.
   *
   * @return {@code null}
   */
  @Override
  public @Nullable GroupResolver parentGroup() {
    return null;
  }

  /**
   * Returns an empty list; an empty entity has no parent group path.
   *
   * @return an empty, unmodifiable list
   */
  @Override
  public List<GroupResolver> parentGroupPath() {
    return List.of();
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
   * <p>Two {@code EmptyEntityResolver} instances are equal if and only if they are anchored to the
   * same {@code root} and {@code globalRoot} references (identity comparison via {@code ==}),
   * mirroring {@link EmptyResolver#equals(Object)}.
   *
   * @param o the object to compare with
   * @return {@code true} if {@code o} is an {@code EmptyEntityResolver} anchored to the same {@code
   *     root} and {@code globalRoot} references as this instance
   */
  @Override
  public boolean equals(Object o) {
    return (o instanceof EmptyEntityResolver that)
        && this.path.root() == that.path.root()
        && this.path.globalRoot() == that.path.globalRoot();
  }
}
