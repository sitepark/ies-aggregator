package com.sitepark.ies.aggregator.resolver;

/**
 * What kind of object an entity is, as far as an aggregator has to tell them apart.
 *
 * <p>The kind decides whether an object has a resource of its own: a page is published as a file,
 * a medium under its binary, while a resource article only carries data that other pages show - a
 * teaser of it cannot refer to it, it has to show it in full.
 */
public enum EntityKind {

  /** An article published as a page of its own. */
  PAGE,

  /** An article that holds data only and is published as no file of its own. */
  RESOURCE,

  /** An article that exists to carry a file; see {@link EntityDescriptor#isMedia()}. */
  MEDIA,

  /** A group of objects, such as a pool. */
  GROUP,

  /** No entity at all - the kind of an empty descriptor. */
  NONE
}
