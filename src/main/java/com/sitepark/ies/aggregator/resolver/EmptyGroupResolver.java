package com.sitepark.ies.aggregator.resolver;

import com.sitepark.ies.aggregator.value.ResolvedValue;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * A {@link GroupResolver} that is always empty — the group counterpart of {@link
 * EmptyEntityResolver}.
 *
 * <p>This class implements the <em>Null Object</em> pattern for group lookups: it is returned when
 * a group cannot be found, so callers can stay on the {@link GroupResolver} type without casting or
 * null-checking. All lookups return defaults or empty results, all entity attributes are neutral
 * ({@link #entityId()} is {@code 0}, {@link #entityType()}/{@link #entityName()}/{@link
 * #entityAnchor()} are empty, {@link #parentGroup()} is {@code null}) and the group itself has no
 * children, no language and is neither a site nor a microsite root.
 *
 * <p>Like {@link EmptyEntityResolver}, it carries the {@link #path()} it was created with, so
 * callers can still navigate back up the tree via {@link #root()} / {@link #globalRoot()}.
 */
public final class EmptyGroupResolver implements GroupResolver {

  private final ResolverPath path;

  /**
   * Creates an empty group resolver anchored to the surrounding navigation context.
   *
   * @param path the surrounding navigation path; must not be {@code null}
   */
  EmptyGroupResolver(ResolverPath path) {
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
   * Returns {@code this}; navigating an empty group resolver always yields an empty resolver.
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
   * Returns {@code 0}; an empty group has no id.
   *
   * @return {@code 0}
   */
  @Override
  public int entityId() {
    return 0;
  }

  /**
   * Returns the empty string; an empty group has no type.
   *
   * @return the empty string
   */
  @Override
  public String entityType() {
    return "";
  }

  /**
   * Returns the empty string; an empty group has no name.
   *
   * @return the empty string
   */
  @Override
  public String entityName() {
    return "";
  }

  /**
   * Returns the empty string; an empty group has no anchor.
   *
   * @return the empty string
   */
  @Override
  public String entityAnchor() {
    return "";
  }

  /**
   * Returns {@code null}; an empty group has no parent group.
   *
   * @return {@code null}
   */
  @Override
  public @Nullable GroupResolver parentGroup() {
    return null;
  }

  /**
   * Returns an empty list; an empty group has no parent group path.
   *
   * @return an empty, unmodifiable list
   */
  @Override
  public List<GroupResolver> parentGroupPath() {
    return List.of();
  }

  /**
   * Returns an empty list; an empty group has no sub-groups.
   *
   * @return an empty, unmodifiable list
   */
  @Override
  public List<GroupResolver> groupSubGroups() {
    return List.of();
  }

  /**
   * Returns an empty list; an empty group contains no entities.
   *
   * @return an empty, unmodifiable list
   */
  @Override
  public List<EntityResolver> groupEntities() {
    return List.of();
  }

  /**
   * Returns an empty list; an empty group has no children.
   *
   * @return an empty, unmodifiable list
   */
  @Override
  public List<EntityResolver> groupChildren() {
    return List.of();
  }

  /**
   * Returns {@code false}; an empty group is not a site root group.
   *
   * @return {@code false}
   */
  @Override
  public boolean isRootSiteGroup() {
    return false;
  }

  /**
   * Returns {@code false}; an empty group is not a microsite root group.
   *
   * @return {@code false}
   */
  @Override
  public boolean isMicrositeRootSiteGroup() {
    return false;
  }

  /**
   * Returns the empty string; an empty group has no language.
   *
   * @return the empty string
   */
  @Override
  public String lang() {
    return "";
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
   * <p>Two {@code EmptyGroupResolver} instances are equal if and only if they are anchored to the
   * same {@code root} and {@code globalRoot} references (identity comparison via {@code ==}),
   * mirroring {@link EmptyEntityResolver#equals(Object)}.
   *
   * @param o the object to compare with
   * @return {@code true} if {@code o} is an {@code EmptyGroupResolver} anchored to the same {@code
   *     root} and {@code globalRoot} references as this instance
   */
  @Override
  public boolean equals(Object o) {
    return (o instanceof EmptyGroupResolver that)
        && this.path.root() == that.path.root()
        && this.path.globalRoot() == that.path.globalRoot();
  }
}
