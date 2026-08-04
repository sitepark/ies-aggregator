package com.sitepark.ies.aggregator.resolver;

import java.util.List;
import org.jspecify.annotations.Nullable;

public interface EntityResolver extends Resolver {

  /**
   * Returns an empty entity resolver anchored to the given navigation path.
   *
   * <p>This is the {@link EntityResolver} counterpart of {@link Resolver#empty(ResolverPath)}: a
   * null-object whose {@link #entityId()} is {@code 0}, whose {@link #entityType()}/{@link
   * #entityName()} are empty and whose {@link #parentGroup()} is {@code null}. It is returned by
   * {@link Resolver#resolveLink(String)} when a link cannot be followed, so callers can stay on the
   * {@code EntityResolver} type and simply test {@link #isEmpty()}.
   *
   * @param path the surrounding navigation path; must not be {@code null}
   * @return an empty entity resolver carrying the given path
   */
  static EntityResolver empty(ResolverPath path) {
    return new EmptyEntityResolver(path);
  }

  /**
   * Returns an empty entity resolver that is its own root.
   *
   * <p>Unlike {@link #empty(ResolverPath)}, which keeps the navigation path of a surrounding
   * object, the returned resolver starts a fresh path: it is its own {@link Resolver#root() root}
   * and {@link Resolver#globalRoot() global root}. This is the null-object for a root lookup that
   * yielded nothing — see {@link com.sitepark.ies.aggregator.port.RootResolverProvider}, whose
   * methods return it when no object matches the given id or anchor.
   *
   * @return an empty, self-rooted entity resolver
   */
  static EntityResolver emptyRoot() {
    return (EntityResolver) ResolverPath.createRoot(EntityResolver::empty);
  }

  int entityId();

  String entityType();

  String entityName();

  String entityAnchor();

  @Nullable GroupResolver parentGroup();

  List<GroupResolver> parentGroupPath();
}
