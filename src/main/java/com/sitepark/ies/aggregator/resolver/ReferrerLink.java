package com.sitepark.ies.aggregator.resolver;

import java.util.Objects;

/**
 * One link that points at an entity, seen from the entity that holds it - an answer of {@link
 * EntityResolver#resolveReferrerLinks(String)}.
 *
 * <p>A referrer that links the entity from several places holds several links: each is answered
 * on its own, with the place it is stored in.
 *
 * @param parent the referring entity; never {@code null}
 * @param linkScope the scope the link field is stored in: the entry of the referrer's list (its
 *     iterate) that holds the link, or the referrer itself when the field is in no list. Its other
 *     fields describe the link - the role a person has for a product, say, or whether the link
 *     is to be published. Never {@code null}.
 */
public record ReferrerLink(EntityResolver parent, Resolver linkScope) {

  /**
   * Creates the link.
   *
   * @throws NullPointerException if an argument is {@code null}
   */
  public ReferrerLink {
    Objects.requireNonNull(parent, "parent");
    Objects.requireNonNull(linkScope, "linkScope");
  }
}
