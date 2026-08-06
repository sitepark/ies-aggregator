package com.sitepark.ies.aggregator.resolver;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

/**
 * When an entity was touched and by whom.
 *
 * <p>Timestamp and editor belong together, so every action an entity went through — created,
 * changed, and whatever is added later — is exposed as one {@code Revision} instead of two separate
 * accessors on {@link EntityDescriptor}.
 *
 * <p>Like the descriptor it belongs to, a revision is a <em>view</em>: both values are resolved when
 * they are read, not when the revision is obtained.
 */
public interface Revision {

  /**
   * Returns the revision that carries no data.
   *
   * <p>Returned by {@link EntityDescriptor#created()} / {@link EntityDescriptor#changed()} of an
   * empty descriptor, so callers can stay on the {@code Revision} type without null-checking it.
   *
   * @return the empty revision
   */
  static Revision empty() {
    return EmptyRevision.INSTANCE;
  }

  /**
   * The point in time of this revision.
   *
   * @return the timestamp, or {@code null} if the action never happened or is unknown
   */
  @Nullable Instant at();

  /**
   * The editor who performed this revision.
   *
   * @return the editor; never {@code null}, {@link Editor#empty()} if the action never happened or
   *     the editor is unknown
   */
  Editor by();
}
