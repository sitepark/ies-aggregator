package com.sitepark.ies.aggregator.resolver;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * A {@link Resolver} whose current scope is a CMS entity.
 *
 * <p>Two kinds of access: the master data of the entity itself via {@link #entity()}, and navigation
 * to surrounding entities via {@link #parentGroup()} / {@link #parentGroupPath()}. Field-by-field
 * reads stay on {@link Resolver}.
 */
public interface EntityResolver extends Resolver {

  /**
   * Returns an empty entity resolver anchored to the given navigation path.
   *
   * <p>This is the {@link EntityResolver} counterpart of {@link Resolver#empty(ResolverPath)}: a
   * null-object whose {@link #entity()} is {@link EntityDescriptor#empty()} and whose {@link
   * #parentGroup()} is {@code null}. It is returned by {@link Resolver#resolveLink(String)} when a
   * link cannot be followed, so callers can stay on the {@code EntityResolver} type and simply test
   * {@link #isEmpty()}.
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
   * yielded nothing — see {@link RootResolverFactory}, whose {@code createByEntity…} methods return
   * it when no entity matches the given id or anchor.
   *
   * @return an empty, self-rooted entity resolver
   */
  static EntityResolver emptyRoot() {
    return (EntityResolver) ResolverPath.createRoot(EntityResolver::empty);
  }

  /**
   * The master data of the entity this resolver reads from.
   *
   * <p>A view, not a snapshot: obtaining it resolves nothing, every field is read on access.
   *
   * @return the entity descriptor; never {@code null}, {@link EntityDescriptor#empty()} if this
   *     resolver is empty
   */
  EntityDescriptor entity();

  /**
   * The group this entity belongs to.
   *
   * @return the parent group, or {@code null} if the entity has none
   */
  @Nullable GroupResolver parentGroup();

  /**
   * The chain of groups from the root down to the parent group of this entity.
   *
   * @return the parent group path; never {@code null}, empty if the entity has no parent group
   */
  List<GroupResolver> parentGroupPath();

  /**
   * The entities whose link field points at this entity - the inverse of {@link
   * Resolver#resolveLinks(String)}.
   *
   * <p>{@code linkField} names the field the way the referring entity stores it, without the
   * iterate path around it: {@code sp_parent_navigation.link} finds a link stored in any entry of
   * {@code sp_parent_navigation_iterate}. Every referring entity appears once; one that cannot be
   * loaded is left out.
   *
   * <p>The answer reflects what the referring entities hold now, not what they published. It is
   * <b>not filtered by publication</b> - a caller that lists only published referrers asks the
   * channel, as it does for any other target. The order is the order of the store and carries no
   * meaning; a caller that needs one sorts.
   *
   * <p>The default implementation knows no inverse index and answers none. A source that can look
   * referrers up overrides it, and registers there whatever makes this entity be generated again
   * when a new referrer appears.
   *
   * @param linkField the link field of the referring entities; must not be {@code null}
   * @return the referring entities; unmodifiable, empty if none points at this entity
   */
  default List<EntityResolver> resolveReferrers(String linkField) {
    return List.of();
  }
}
