package com.sitepark.ies.aggregator.resolver;

/**
 * Factory that creates a fresh root {@link Resolver} per aggregation run.
 *
 * <p>Each {@code create} call produces a new instance, ensuring that mutable aggregator state does
 * not leak between generations. Two variants exist:
 *
 * <ul>
 *   <li>{@link #create(int)} starts a standalone resolver tree — the created resolver is its own
 *       {@link Resolver#root() root} and {@link Resolver#globalRoot() global root} and its {@link
 *       Resolver#path() path} begins fresh.
 *   <li>{@link #create(ResolverPath, int)} starts a new root <em>within</em> an existing navigation
 *       chain — the created resolver becomes a new global root, yet remains part of the given
 *       parent path.
 * </ul>
 */
public interface RootResolverFactory {

  /**
   * Creates a fresh standalone root {@link Resolver} for the object with the given id.
   *
   * <p>The created resolver has no parent: it is its own {@link Resolver#root()} and {@link
   * Resolver#globalRoot()}, and its {@link Resolver#path()} starts fresh.
   *
   * @param id of the article to aggregate the resource for
   * @return a fresh {@link Resolver} instance, rooted via {@link ResolverPath#createRoot}
   */
  Resolver create(int id);

  /**
   * Creates a fresh root {@link Resolver} for the object with the given id, anchored within an
   * existing navigation chain.
   *
   * <p>The created resolver becomes a new {@link Resolver#root() root} and {@link
   * Resolver#globalRoot() global root}, but is appended to {@code path} so the navigation history is
   * preserved.
   *
   * @param path the parent resolver path the new resolver is anchored to
   * @param id of the article to aggregate the resource for
   * @return a fresh {@link Resolver} instance, rooted via {@link
   *     ResolverPath#enterRoot(ResolverPath.Factory)}
   */
  Resolver create(ResolverPath path, int id);
}
