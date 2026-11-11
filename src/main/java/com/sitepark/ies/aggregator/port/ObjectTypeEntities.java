package com.sitepark.ies.aggregator.port;

import com.sitepark.ies.aggregator.resolver.EntityResolver;
import java.util.List;

/**
 * Finds all entities of one object type.
 *
 * <p>This is the port for the legacy SPML idiom {@code <sp:collection query="objectType =
 * '<type>'"/>}: a list, a feed or an overview that is not fed by links but by <em>everything of a
 * kind</em> - all notices, all planing themes. Neither the current object nor a link leads there;
 * only the platform, which can query the whole repository, can answer.
 *
 * <p>The answer is what the tag answers during a generation: the entities of the current tenant
 * that are published in the channel being generated. Groups are no entities of an object type. The
 * search is not limited and not checked against a user's permissions, and its order is the order of
 * the store, which carries no meaning - a caller that needs one sorts. Outside a generation, where
 * no channel is being generated, nothing is filtered by publication.
 *
 * <p>Whoever asks is to be generated again when an entity of the type appears or disappears, so an
 * implementation records the search as a dependency of the object being generated, as the tag does.
 */
@FunctionalInterface
public interface ObjectTypeEntities {

  /**
   * Returns every entity whose object type is the given one.
   *
   * <p>Each entity comes back as a root {@link EntityResolver}. One that cannot be loaded is left
   * out. A type nobody uses, and a name that is no object type at all, yield an empty list.
   *
   * @param objectType the object type, as it is written in the {@code objectType} field - e.g.
   *     {@code notice}
   * @return the entities, in the order of the store; empty if there is none
   */
  List<EntityResolver> of(String objectType);
}
