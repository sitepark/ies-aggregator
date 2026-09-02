package com.sitepark.ies.aggregator.resolver;

/**
 * A user of the system, as far as generated output is concerned.
 *
 * <p>Reached in two ways: as the one who performed a {@link Revision} of an entity, and as the
 * target of a field linking to a user account ({@link Resolver#resolveUser(String)}). Both want the
 * same handful of fields, so both answer this type.
 *
 * <p>Deliberately minimal: only the data a template or assembler needs to name a person. It is not
 * a security principal — permissions, roles and authentication live outside this API.
 */
public interface User {

  /**
   * Returns the user that carries no data.
   *
   * <p>The null object for an unknown user: every field is empty. Returned by {@link
   * Revision#by()} when the action never happened or the user is unknown, so callers can stay on
   * the {@code User} type without null-checking it.
   *
   * @return the empty user
   */
  static User empty() {
    return EmptyUser.INSTANCE;
  }

  /**
   * The id of the user.
   *
   * <p>Cheap to read: implementations must not resolve anything here.
   *
   * @return the user id, or the empty string if unknown
   */
  String id();

  /**
   * The anchor of the user — the stable, human-readable address of the user account.
   *
   * <p>Resolved on access, like {@link #name()}.
   *
   * @return the anchor, or the empty string if the user has none or cannot be resolved
   */
  String anchor();

  /**
   * The display name of the user.
   *
   * <p>Resolved on access, not when the surrounding {@link EntityDescriptor} is created — looking up
   * a name for an id can be expensive, and callers that never read it must not pay for it.
   * Implementations may memoize the result, so callers can invoke this repeatedly.
   *
   * @return the display name, or the empty string if it cannot be resolved
   */
  String name();

  /**
   * The given name of the user.
   *
   * <p>Resolved on access, like {@link #name()}. Kept apart from {@link #lastName()} because
   * consumers order and abbreviate the two parts differently; {@link #name()} is the ready-made
   * display form for everyone else.
   *
   * @return the given name, or the empty string if it cannot be resolved
   */
  String firstName();

  /**
   * The family name of the user.
   *
   * <p>Resolved on access, like {@link #name()}.
   *
   * @return the family name, or the empty string if it cannot be resolved
   */
  String lastName();
}
