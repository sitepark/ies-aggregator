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
   * #type()}/{@link #qualifiedId()}/{@link #version()}/{@link #name()}/{@link #anchor()} are empty
   * and both revisions are {@link Revision#empty()}.
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
   * The id of the entity as the source system writes it, qualified by everything that {@link #id()}
   * drops — the client the entity belongs to and its object type.
   *
   * <p>Where {@link #id()} is the plain number that identifies the entity within its repository,
   * this is the form a consumer stores to address the very same entity again from outside, and the
   * form that appears in exported data. Its syntax belongs to the source system and is opaque here.
   *
   * @return the qualified id, or the empty string if the entity is empty
   */
  String qualifiedId();

  /**
   * The revision of the entity, identifying the state its data is in.
   *
   * <p>Changes with every edit, so a consumer can tell two states of the same entity apart — for
   * cache keys, for instance. Opaque and only comparable for equality; no ordering is implied.
   *
   * @return the version, or the empty string if unknown
   */
  String version();

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
   * The file name the entity is published under, without a path.
   *
   * <p>Not every entity has one: it belongs to what the CMS publishes as a file, so a pool answers
   * the empty string. Consumers use it where the name of the published file carries meaning of its
   * own — an error page named {@code 404}, say.
   *
   * @return the file name, or the empty string if the entity is not published as a file
   */
  String filename();

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
