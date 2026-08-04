package com.sitepark.ies.aggregator.port;

import com.sitepark.ies.aggregator.resolver.EntityResolver;
import com.sitepark.ies.aggregator.resolver.Resolver;
import com.sitepark.ies.aggregator.resolver.ResolverPath;

/**
 * Provides a root {@link EntityResolver} for an arbitrary object, addressed by id or anchor.
 *
 * <p>Aggregators and assemblers normally reach data in two ways: through the {@link Resolver} handed
 * to them as {@code source}, and by navigating its fields via {@link Resolver#resolve(String)} /
 * {@link Resolver#resolveLink(String)}. This port adds a third way for objects that are <em>not</em>
 * linked from the current one — for example a portal page configured by anchor. It is injected via
 * {@code @Inject} like any other port.
 *
 * <h2>Fresh root</h2>
 *
 * <p>Every returned resolver starts a brand-new resolver tree: it is its own {@link Resolver#root()
 * root} and {@link Resolver#globalRoot() global root}, and its {@link Resolver#path() path} begins
 * fresh (see {@link ResolverPath#createRoot(ResolverPath.Factory)}). It inherits neither the scope nor
 * the path of the caller, so navigating up from it never leads back into the calling object.
 *
 * <h2>Unknown targets</h2>
 *
 * <p>A missing object is a normal case, not an error: a target may have been deleted or an anchor may
 * be misspelled in an editorial field. Both methods therefore return an empty, self-rooted {@link
 * EntityResolver} instead of {@code null} or an exception — test with {@link Resolver#isEmpty()}, and
 * see {@link EntityResolver#emptyRoot()} for implementations.
 *
 * <h2>Relation to {@code RootResolverFactory}</h2>
 *
 * <p>{@link com.sitepark.ies.aggregator.resolver.RootResolverFactory} is used by the IES runtime to
 * start a generation run and can anchor a new root inside an existing {@link ResolverPath}. This port
 * is used by aggregation code <em>during</em> a run, always yields a standalone root, and also
 * resolves anchors.
 *
 * @see EntityResolver
 * @see com.sitepark.ies.aggregator.resolver.RootResolverFactory
 */
public interface RootResolverProvider {

  /**
   * Returns the object with the given id as a root {@link EntityResolver}.
   *
   * <p>The returned resolver is never {@code null}; if no object with that id exists, an empty
   * self-rooted {@link EntityResolver} is returned, so callers never need to null-check.
   *
   * @param id the id of the object to read
   * @return the object as a fresh root resolver, or an empty {@link EntityResolver} if no such object
   *     exists
   * @see EntityResolver#emptyRoot()
   */
  EntityResolver getById(int id);

  /**
   * Returns the object with the given anchor as a root {@link EntityResolver}.
   *
   * <p>An anchor is the stable, human-readable alias of an object (e.g. {@code "hauptseite"}) as
   * modelled by {@code com.sitepark.ies.sharedkernel.anchor.Anchor}. It is the addressing of choice
   * for objects referenced from configuration, because it survives copying and re-importing, while an
   * id does not.
   *
   * <p>The returned resolver is never {@code null}; if no object carries that anchor, an empty
   * self-rooted {@link EntityResolver} is returned.
   *
   * @param anchor the anchor of the object to read; must not be {@code null}
   * @return the object as a fresh root resolver, or an empty {@link EntityResolver} if no object
   *     carries the given anchor
   * @see EntityResolver#emptyRoot()
   */
  EntityResolver getByAnchor(String anchor);
}
