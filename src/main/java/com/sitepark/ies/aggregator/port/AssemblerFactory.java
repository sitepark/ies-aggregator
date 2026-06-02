package com.sitepark.ies.aggregator.port;

import com.sitepark.ies.aggregator.Assembler;

/**
 * Factory that creates a fresh assembler instance per aggregation run.
 *
 * <p>Each call to {@link #create(String, Class)} produces a new instance, ensuring that mutable
 * assembler state does not leak between generations.
 *
 * <p>Assemblers are looked up by a string key that is declared via the {@link Assembler @Assembler}
 * annotation on the implementing class. The same key may be declared by multiple implementations;
 * in that case the implementation with the highest {@link Assembler#priority() priority} wins. This
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
   * <p>The key is matched against the {@link Assembler#value() value} of the {@code @Assembler}
   * annotation on registered assembler classes. If multiple classes declare the same key, the one
   * with the highest {@link Assembler#priority() priority} is used.
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
}
