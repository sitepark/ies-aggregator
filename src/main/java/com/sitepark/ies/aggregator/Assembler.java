package com.sitepark.ies.aggregator;

import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Assembles a value of type {@code V} from a request of type {@code REQ}.
 *
 * <p>Assemblers registered under the same key (via {@link AssemblerBinding}) form a chain (see {@link
 * com.sitepark.ies.aggregator.port.AssemblerChain}): each assembler receives the value produced by
 * the preceding one via {@code previous} and may enrich or replace it. The first assembler in the
 * chain receives {@code null}.
 *
 * @param <REQ> the request type carrying the assembler's inputs
 * @param <V> the assembled value type
 */
public interface Assembler<REQ, V> {

  /**
   * Assembles the value from the given request.
   *
   * @param request the assembler inputs
   * @param previous the value assembled by the preceding chain element, or {@code null} for the
   *     first
   * @return the assembled value, or {@link Optional#empty()} if nothing was produced
   */
  Optional<V> assemble(REQ request, @Nullable V previous);
}
