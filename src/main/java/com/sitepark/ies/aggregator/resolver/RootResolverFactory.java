package com.sitepark.ies.aggregator.resolver;

/**
 * Creates root {@link Resolver} instances for objects addressed by id or anchor.
 *
 * <p>Aggregators and assemblers normally reach data in two ways: through the {@link Resolver} handed
 * to them as {@code source}, and by navigating its fields via {@link Resolver#resolve(String)} /
 * {@link Resolver#resolveLink(String)}. This factory adds a third way for objects that are
 * <em>not</em> linked from the current one — for example a portal page configured by anchor. It is
 * also the entry point the IES runtime uses to start a generation run.
 *
 * <h2>Fresh root</h2>
 *
 * <p>Every {@code createBy…} call produces a new instance, ensuring that mutable aggregator state
 * does not leak between generations. Two variants exist:
 *
 * <ul>
 *   <li>The variants without a {@link ResolverPath} start a standalone resolver tree — the created
 *       resolver is its own {@link Resolver#root() root} and {@link Resolver#globalRoot() global
 *       root} and its {@link Resolver#path() path} begins fresh. It inherits neither the scope nor
 *       the path of the caller, so navigating up from it never leads back into the calling object.
 *   <li>The variants taking a {@link ResolverPath} start a new root <em>within</em> an existing
 *       navigation chain — the created resolver becomes a new global root, yet remains part of the
 *       given parent path.
 * </ul>
 *
 * <h2>Addressing by anchor</h2>
 *
 * <p>An anchor is the stable, human-readable alias of an object (e.g. {@code "hauptseite"}) as
 * modelled by {@code com.sitepark.ies.sharedkernel.anchor.Anchor}. It is the addressing of choice
 * for objects referenced from configuration, because it survives copying and re-importing, while an
 * id does not.
 */
public interface RootResolverFactory {

  /**
   * Creates a fresh standalone root {@link EntityResolver} for the entity with the given id.
   *
   * <p>The created resolver has no parent: it is its own {@link Resolver#root()} and {@link
   * Resolver#globalRoot()}, and its {@link Resolver#path()} starts fresh.
   *
   * <p>A missing entity is a normal case, not an error: the returned resolver is never {@code
   * null}; if no entity with that id exists, an empty self-rooted {@link EntityResolver} is
   * returned, so callers never need to null-check. Test with {@link Resolver#isEmpty()}.
   *
   * @param id the id of the entity to read
   * @return a fresh {@link EntityResolver} instance, rooted via {@link ResolverPath#createRoot},
   *     or an empty {@link EntityResolver} if no such entity exists
   * @see EntityResolver#emptyRoot()
   */
  EntityResolver createByEntityId(int id);

  /**
   * Creates a fresh standalone root {@link EntityResolver} for the entity with the given anchor.
   *
   * <p>Behaves like {@link #createByEntityId(int)}, but addresses the entity by its anchor; if no
   * entity carries that anchor, an empty self-rooted {@link EntityResolver} is returned.
   *
   * @param anchor the anchor of the entity to read; must not be {@code null}
   * @return a fresh {@link EntityResolver} instance, rooted via {@link ResolverPath#createRoot},
   *     or an empty {@link EntityResolver} if no entity carries the given anchor
   * @see EntityResolver#emptyRoot()
   */
  EntityResolver createByEntityAnchor(String anchor);

  /**
   * Creates a fresh root {@link EntityResolver} for the entity with the given id, anchored within
   * an existing navigation chain.
   *
   * <p>The created resolver becomes a new {@link Resolver#root() root} and {@link
   * Resolver#globalRoot() global root}, but is appended to {@code path} so the navigation history
   * is preserved.
   *
   * <p>A missing entity is a normal case, not an error: the returned resolver is never {@code
   * null}; if no entity with that id exists, an empty {@link EntityResolver} carrying {@code path}
   * is returned, so the navigation history stays intact and callers never need to null-check. Test
   * with {@link Resolver#isEmpty()}.
   *
   * @param path the parent resolver path the new resolver is anchored to
   * @param id the id of the entity to read
   * @return a fresh {@link EntityResolver} instance, rooted via {@link
   *     ResolverPath#enterRoot(ResolverPath.Factory)}, or an empty {@link EntityResolver} carrying
   *     {@code path} if no such entity exists
   * @see EntityResolver#empty(ResolverPath)
   */
  EntityResolver createByEntityId(ResolverPath path, int id);

  /**
   * Creates a fresh standalone root {@link GroupResolver} for the group with the given id.
   *
   * <p>The group counterpart of {@link #createByEntityId(int)}: the created resolver is its own
   * {@link Resolver#root()} and {@link Resolver#globalRoot()}, and its {@link Resolver#path()}
   * starts fresh.
   *
   * <p>A missing group is a normal case, not an error: the returned resolver is never {@code null};
   * if no group with that id exists, an empty self-rooted {@link GroupResolver} is returned, so
   * callers never need to null-check. Test with {@link Resolver#isEmpty()}.
   *
   * @param id the id of the group to read
   * @return a fresh {@link GroupResolver} instance, rooted via {@link ResolverPath#createRoot}, or
   *     an empty {@link GroupResolver} if no such group exists
   * @see GroupResolver#emptyRoot()
   */
  GroupResolver createByGroupId(int id);

  /**
   * Creates a fresh standalone root {@link GroupResolver} for the group with the given anchor.
   *
   * <p>Behaves like {@link #createByGroupId(int)}, but addresses the group by its anchor; if no
   * group carries that anchor, an empty self-rooted {@link GroupResolver} is returned.
   *
   * @param anchor the anchor of the group to read; must not be {@code null}
   * @return a fresh {@link GroupResolver} instance, rooted via {@link ResolverPath#createRoot}, or
   *     an empty {@link GroupResolver} if no group carries the given anchor
   * @see GroupResolver#emptyRoot()
   */
  GroupResolver createByGroupAnchor(String anchor);

  /**
   * Creates a fresh root {@link GroupResolver} for the group with the given id, anchored within an
   * existing navigation chain.
   *
   * <p>The created resolver becomes a new {@link Resolver#root() root} and {@link
   * Resolver#globalRoot() global root}, but is appended to {@code path} so the navigation history
   * is preserved.
   *
   * <p>A missing group is a normal case, not an error: the returned resolver is never {@code null};
   * if no group with that id exists, an empty {@link GroupResolver} carrying {@code path} is
   * returned, so the navigation history stays intact and callers never need to null-check. Test
   * with {@link Resolver#isEmpty()}.
   *
   * @param path the parent resolver path the new resolver is anchored to
   * @param id the id of the group to read
   * @return a fresh {@link GroupResolver} instance, rooted via {@link
   *     ResolverPath#enterRoot(ResolverPath.Factory)}, or an empty {@link GroupResolver} carrying
   *     {@code path} if no such group exists
   * @see GroupResolver#empty(ResolverPath)
   */
  GroupResolver createByGroupId(ResolverPath path, int id);
}
