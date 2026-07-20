package com.sitepark.ies.aggregator.port;

import com.sitepark.ies.aggregator.AssemblerBinding;

/**
 * Factory that creates a fresh assembler instance per aggregation run.
 *
 * <p>Each call to {@link #create(String, Class)} produces a new instance, ensuring that mutable
 * assembler state does not leak between generations.
 *
 * <p>Assemblers are looked up by a string key that is declared via the {@link AssemblerBinding @AssemblerBinding}
 * annotation on the implementing class. The same key may be declared by multiple implementations;
 * in that case the implementation with the highest {@link AssemblerBinding#priority() priority} wins. This
 * allows customer-specific libraries to override built-in assemblers without code changes.
 *
 * <p>The expected return type is passed explicitly as a {@link Class} so that the caller receives a
 * type-safe reference. The factory verifies at runtime that the resolved assembler is assignable to
 * the requested type.
 *
 * <pre>{@code
 * InternalLinkAssembler assembler = factory.create("link.internal", InternalLinkAssembler.class);
 * }</pre>
 */
public interface AssemblerFactory {
  /**
   * Creates a new assembler instance for the given key.
   *
   * <p>The key is matched against the {@link AssemblerBinding#value() value} of the {@code @AssemblerBinding}
   * annotation on registered assembler classes. If multiple classes declare the same key, the one
   * with the highest {@link AssemblerBinding#priority() priority} is used.
   *
   * @param <T> the expected assembler type
   * @param key the key identifying the assembler to create
   * @param clazz the expected type of the assembler; the resolved instance must be assignable to
   *     this type
   * @return a fresh assembler instance of type {@code T}
   * @throws IllegalArgumentException if no assembler is registered for the given key, or if the
   *     resolved assembler is not assignable to {@code clazz}
   */
  <T> T create(String key, Class<T> clazz);

  /**
   * Creates the chain of assemblers registered for the given key, ordered for sequential execution.
   *
   * <p>Unlike {@link #create(String, Class)}, which selects a single winner by priority, this method
   * returns <em>all</em> assemblers declaring the key and assignable to {@code clazz}, each as a
   * fresh instance, ordered by ascending {@link AssemblerBinding#priority() priority}. Callers thread the
   * assembled value through the chain: the first (lowest-priority, typically built-in) assembler
   * produces the base value, and each following assembler receives the previous result and may
   * enrich or replace it. Higher priority therefore means "runs later / has the last word".
   *
   * <p>Two {@code @AssemblerBinding} attributes prune the chain; pruned assemblers are neither executed nor
   * instantiated:
   *
   * <ul>
   *   <li>{@link AssemblerBinding#chainRoot() chainRoot} — the highest-priority root becomes the effective
   *       start; all assemblers with a lower priority are dropped.
   *   <li>{@link AssemblerBinding#chainBreak() chainBreak} — the lowest-priority break becomes the effective
   *       end; all assemblers with a higher priority are dropped.
   * </ul>
   *
   * <p>If a {@code chainBreak} sits below a {@code chainRoot} the resulting window is empty; the
   * chain is then empty. An empty chain is a valid result — no exception is thrown when nothing is
   * registered for the key.
   *
   * @param <T> the expected assembler type
   * @param key the key identifying the assemblers to chain
   * @param clazz the expected type of the assemblers; each resolved instance must be assignable to
   *     this type
   * @return the ordered chain of fresh assembler instances, possibly empty
   */
  <T> AssemblerChain<T> createChain(String key, Class<T> clazz);
}
