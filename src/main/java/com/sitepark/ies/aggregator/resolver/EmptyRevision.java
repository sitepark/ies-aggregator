package com.sitepark.ies.aggregator.resolver;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

/**
 * A {@link Revision} that carries no data — the null object returned by {@link Revision#empty()}.
 *
 * <p>Stateless, therefore a single shared {@link #INSTANCE}.
 */
final class EmptyRevision implements Revision {

  static final EmptyRevision INSTANCE = new EmptyRevision();

  private EmptyRevision() {}

  /**
   * Returns {@code null}; an empty revision has no timestamp.
   *
   * @return {@code null}
   */
  @Override
  public @Nullable Instant at() {
    return null;
  }

  /**
   * Returns the empty user; an empty revision has no user.
   *
   * @return {@link User#empty()}
   */
  @Override
  public User by() {
    return User.empty();
  }
}
