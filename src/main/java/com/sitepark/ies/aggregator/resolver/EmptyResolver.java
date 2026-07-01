package com.sitepark.ies.aggregator.resolver;

import com.sitepark.ies.aggregator.value.ResolvedValue;
import java.util.List;

/**
 * A {@link Resolver} that is always empty — all lookups return defaults or empty results.
 *
 * <p>Used as a null-object substitute when no real data source is available. Accessible via {@link
 * Resolver#empty()}.
 */
public final class EmptyResolver implements Resolver {

  static final Resolver INSTANCE = new EmptyResolver();

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public List<Resolver> resolveList(String s) {
    return List.of();
  }

  @Override
  public Resolver resolve(String s) {
    return this;
  }

  @Override
  public ResolvedValue value(String s) {
    return ResolvedValue.empty();
  }
}
