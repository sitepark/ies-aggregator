package com.sitepark.ies.aggregator.resolver;

/**
 * The user who performed a {@link Revision} of an entity.
 *
 * <p>Deliberately minimal: only the data a template or assembler needs to attribute a change. It is
 * not a security principal — permissions, roles and authentication live outside this API.
 */
public interface Editor {

  /**
   * The id of the editor.
   *
   * <p>Cheap to read: implementations must not resolve anything here.
   *
   * @return the editor id, or the empty string if unknown
   */
  String id();

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
}
