package com.sitepark.ies.aggregator;

import com.sitepark.ies.aggregator.resolver.Resolver;

/**
 * A custom applicability rule for an assembler, referenced via {@link AssemblerBinding#condition()}.
 *
 * <p>Used for the rare cases in which a declarative {@link AssemblerBinding#objectTypes() objectTypes}
 * match is not expressive enough. The {@link com.sitepark.ies.aggregator.port.AssemblerFactory}
 * instantiates the condition via dependency injection (so it may declare its own constructor
 * dependencies) and calls {@link #appliesTo(Resolver)} with the resolver of the current aggregation
 * scope. The assembler is only considered when the condition returns {@code true}.
 */
public interface AssemblerCondition {

  /**
   * Decides whether the annotated assembler applies in the current scope.
   *
   * @param source the resolver of the current aggregation scope
   * @return {@code true} if the assembler applies, {@code false} to exclude it
   */
  boolean appliesTo(Resolver source);

  /**
   * The default condition: always applies. Serves as the {@link AssemblerBinding#condition()}
   * default so the common case needs no class; the factory recognises it and never instantiates it.
   */
  final class Always implements AssemblerCondition {
    @Override
    public boolean appliesTo(Resolver source) {
      return true;
    }
  }
}
