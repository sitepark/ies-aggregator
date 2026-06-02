package com.sitepark.ies.aggregator.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import com.sitepark.ies.aggregator.value.ResolvedValue;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ResolverTest {

  /** Minimal {@link Resolver} backed by a fixed map, used to exercise the default methods. */
  private static Resolver resolverWith(Map<String, ResolvedValue> values) {
    return new Resolver() {
      @Override
      public boolean isEmpty() {
        return values.isEmpty();
      }

      @Override
      public List<Resolver> resolveList(String key) {
        return List.of();
      }

      @Override
      public Resolver resolve(String key) {
        return Resolver.EMPTY;
      }

      @Override
      public ResolvedValue value(String key) {
        return values.getOrDefault(key, ResolvedValue.EMPTY);
      }
    };
  }

  @Test
  void coalesceReturnsFirstNonEmptyValue() {
    Resolver resolver = resolverWith(Map.of("b", ResolvedValue.of("second")));

    assertThat(resolver.coalesce("a", "b", "c").asString())
        .as("coalesce() should return the value of the first key that resolves to a value")
        .isEqualTo("second");
  }

  @Test
  void coalesceSkipsEmptyValuesInOrder() {
    Resolver resolver =
        resolverWith(Map.of("a", ResolvedValue.of("first"), "b", ResolvedValue.of("second")));

    assertThat(resolver.coalesce("a", "b").asString())
        .as("coalesce() should prefer the earliest key, not a later one")
        .isEqualTo("first");
  }

  @Test
  void coalesceReturnsEmptyWhenNoKeyResolves() {
    Resolver resolver = resolverWith(Map.of());

    assertThat(resolver.coalesce("a", "b").isEmpty())
        .as("coalesce() should return an empty value when no key resolves")
        .isTrue();
  }
}
