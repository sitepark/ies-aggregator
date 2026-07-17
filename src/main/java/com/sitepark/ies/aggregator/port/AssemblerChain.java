package com.sitepark.ies.aggregator.port;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import org.jspecify.annotations.Nullable;

/**
 * The ordered chain of assemblers registered under a key, as returned by {@link
 * AssemblerFactory#createChain}.
 *
 * <p>Assemblers run in ascending {@link com.sitepark.ies.aggregator.Assembler#priority() priority}
 * order. A value is threaded through the chain via {@link #fold}: the first assembler produces the
 * base value and each following assembler receives the previous result and may enrich or replace it.
 *
 * <p>Callers whose {@code assemble} method has a uniform shape use {@link #fold}; callers that need
 * the raw sequence (e.g. to bridge generic wildcards) iterate the chain directly, as it is {@link
 * Iterable}.
 *
 * @param <T> the assembler type
 */
public final class AssemblerChain<T> implements Iterable<T> {

  private final List<T> assemblers;

  public AssemblerChain(List<T> assemblers) {
    this.assemblers = List.copyOf(assemblers);
  }

  /**
   * Threads a value through the chain. Starting from {@code null}, each assembler is invoked with the
   * value produced so far and returns the next value; the first assembler therefore receives {@code
   * null} as its previous value.
   *
   * @param <V> the assembled value type
   * @param step invokes a single assembler with the value produced so far and returns its result
   * @return the value after the last assembler, or {@link Optional#empty()} for an empty chain
   */
  public <V> Optional<V> fold(BiFunction<T, @Nullable V, Optional<V>> step) {
    @Nullable V value = null;
    for (T assembler : this.assemblers) {
      value = step.apply(assembler, value).orElse(null);
    }
    return Optional.ofNullable(value);
  }

  /** Returns the assemblers in execution order. */
  public List<T> assemblers() {
    return this.assemblers;
  }

  /** Returns {@code true} if no assembler is registered for the key. */
  public boolean isEmpty() {
    return this.assemblers.isEmpty();
  }

  @Override
  public Iterator<T> iterator() {
    return this.assemblers.iterator();
  }
}
