package com.sitepark.ies.aggregator.resolver;

/**
 * The master data of the entity an {@link EntityResolver} reads from.
 *
 * <p>Deliberately not the whole entity: only the few fields that describe it — identity, naming and
 * the revisions it went through. Everything else is read field by field through {@link
 * Resolver#value(String)} and {@link Resolver#resolve(String)}.
 *
 * <p>A descriptor is a <em>view</em>, not a snapshot: every value is resolved when it is read, so
 * obtaining a descriptor costs nothing and callers only pay for what they actually use. New master
 * data fields are added here rather than as new methods on {@link EntityResolver}.
 */
public interface EntityDescriptor {

  /**
   * Returns the descriptor that carries no data.
   *
   * <p>Returned by {@link EntityResolver#entity()} of an empty resolver, so callers can stay on the
   * {@code EntityDescriptor} type without null-checking it: {@link #id()} is {@code 0}, {@link
   * #type()}/{@link #name()}/{@link #anchor()} are empty and both revisions are {@link
   * Revision#empty()}.
   *
   * @return the empty descriptor
   */
  static EntityDescriptor empty() {
    return EmptyEntityDescriptor.INSTANCE;
  }

  /**
   * The id of the entity.
   *
   * @return the entity id, or {@code 0} if the entity is empty
   */
  int id();

  /**
   * The object type of the entity, e.g. {@code content} or {@code group}.
   *
   * @return the object type, or the empty string if unknown
   */
  String type();

  /**
   * The name of the entity.
   *
   * @return the entity name, or the empty string if unknown
   */
  String name();

  /**
   * The anchor of the entity — its stable, human-readable address.
   *
   * @return the anchor, or the empty string if the entity has none
   */
  String anchor();

  /**
   * When the entity was created, and by whom.
   *
   * @return the creating revision; never {@code null}, {@link Revision#empty()} if unknown
   */
  Revision created();

  /**
   * When the entity was last changed, and by whom.
   *
   * @return the last change; never {@code null}, {@link Revision#empty()} if the entity was never
   *     changed
   */
  Revision changed();
}
