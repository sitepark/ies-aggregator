package com.sitepark.ies.aggregator.resolver;

import java.util.List;

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

  List<GroupResolver> groupSubGroups();

  List<EntityResolver> groupEntities();

  List<EntityResolver> groupChildren();

  boolean isRootSiteGroup();

  boolean isMicrositeRootSiteGroup();

  String lang();

  default boolean isGroupPathRoot() {
    return parentGroup() == null;
  }
}
