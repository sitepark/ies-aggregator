package com.sitepark.ies.aggregator.resolver;

import java.util.List;

/**
 * An {@link EntityResolver} whose current scope is a group.
 *
 * <p>{@link #entity()} narrows to {@link GroupDescriptor}, so the group-specific master data is
 * reached through the same object as the shared entity fields. The methods declared here navigate to
 * the entities below the group.
 */
public interface GroupResolver extends EntityResolver {

  /**
   * Returns an empty group resolver anchored to the given navigation path.
   *
   * <p>This is the {@link GroupResolver} counterpart of {@link EntityResolver#empty(ResolverPath)}:
   * a null-object whose entity attributes are neutral and which has no sub-groups, no entities, no
   * children and no language. Callers can stay on the {@code GroupResolver} type and simply test
   * {@link #isEmpty()}.
   *
   * @param path the surrounding navigation path; must not be {@code null}
   * @return an empty group resolver carrying the given path
   */
  static GroupResolver empty(ResolverPath path) {
    return new EmptyGroupResolver(path);
  }

  /**
   * Returns an empty group resolver that is its own root.
   *
   * <p>Unlike {@link #empty(ResolverPath)}, which keeps the navigation path of a surrounding
   * object, the returned resolver starts a fresh path: it is its own {@link Resolver#root() root}
   * and {@link Resolver#globalRoot() global root}. This is the null-object for a root lookup that
   * yielded nothing — see {@link RootResolverFactory}, whose {@code createByGroup…} methods return
   * it when no group matches the given id or anchor.
   *
   * @return an empty, self-rooted group resolver
   */
  static GroupResolver emptyRoot() {
    return (GroupResolver) ResolverPath.createRoot(GroupResolver::empty);
  }

  /**
   * The master data of the group this resolver reads from.
   *
   * <p>Narrows {@link EntityResolver#entity()} to {@link GroupDescriptor}, which adds the
   * group-specific fields to the shared entity fields.
   *
   * @return the group descriptor; never {@code null}, {@link GroupDescriptor#empty()} if this
   *     resolver is empty
   */
  @Override
  GroupDescriptor entity();

  /**
   * The groups directly below this group.
   *
   * @return the sub-groups; never {@code null}, empty if the group has none
   */
  List<GroupResolver> subGroups();

  /**
   * The entities directly below this group, without its sub-groups.
   *
   * @return the entities; never {@code null}, empty if the group has none
   */
  List<EntityResolver> entities();

  /**
   * Everything directly below this group — its sub-groups and its entities.
   *
   * @return the children; never {@code null}, empty if the group has none
   */
  List<EntityResolver> children();

  /**
   * Whether this group is the topmost group of its path.
   *
   * @return {@code true} if the group has no parent group
   */
  default boolean isPathRoot() {
    return parentGroup() == null;
  }
}
