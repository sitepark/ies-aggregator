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
   * Returns the empty editor; an empty revision has no editor.
   *
   * @return {@link Editor#empty()}
   */
  @Override
  public Editor by() {
    return Editor.empty();
  }
}
