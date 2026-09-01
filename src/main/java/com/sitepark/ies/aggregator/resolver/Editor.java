package com.sitepark.ies.aggregator.resolver;

/**
 * The user who performed a {@link Revision} of an entity.
 *
 * <p>Deliberately minimal: only the data a template or assembler needs to attribute a change. It is
 * not a security principal — permissions, roles and authentication live outside this API.
 */
public interface Editor {

  /**
   * Returns the editor that carries no data.
   *
   * <p>The null object for an unknown editor: every field is empty. Returned by {@link
   * Revision#by()} when the action never happened or the editor is unknown, so callers can stay on
   * the {@code Editor} type without null-checking it.
   *
   * @return the empty editor
   */
  static Editor empty() {
    return EmptyEditor.INSTANCE;
  }

  /**
   * The id of the editor.
   *
   * <p>Cheap to read: implementations must not resolve anything here.
   *
   * @return the editor id, or the empty string if unknown
   */
  String id();

  /**
   * The anchor of the editor — the stable, human-readable address of the user account.
   *
   * <p>Resolved on access, like {@link #name()}.
   *
   * @return the anchor, or the empty string if the editor has none or cannot be resolved
   */
  String anchor();

  /**
   * The display name of the editor.
   *
   * <p>Resolved on access, not when the surrounding {@link EntityDescriptor} is created — looking up
   * a name for an id can be expensive, and callers that never read it must not pay for it.
   * Implementations may memoize the result, so callers can invoke this repeatedly.
   *
   * @return the display name, or the empty string if it cannot be resolved
   */
  String name();

  /**
   * The given name of the editor.
   *
   * <p>Resolved on access, like {@link #name()}. Kept apart from {@link #lastName()} because
   * consumers order and abbreviate the two parts differently; {@link #name()} is the ready-made
   * display form for everyone else.
   *
   * @return the given name, or the empty string if it cannot be resolved
   */
  String firstName();

  /**
   * The family name of the editor.
   *
   * <p>Resolved on access, like {@link #name()}.
   *
   * @return the family name, or the empty string if it cannot be resolved
   */
  String lastName();
}
