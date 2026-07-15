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

  int entityId();

  String entityType();

  String entityName();

  @Nullable GroupResolver parentGroup();

  List<GroupResolver> parentGroupPath();
}
