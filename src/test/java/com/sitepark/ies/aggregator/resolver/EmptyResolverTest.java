package com.sitepark.ies.aggregator.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import com.sitepark.ies.aggregator.value.ResolvedValue;
import org.junit.jupiter.api.Test;

class EmptyResolverTest {

  @Test
  void isAlwaysEmpty() {
    assertThat(new EmptyResolver().isEmpty())
        .as("EmptyResolver should always report itself as empty")
        .isTrue();
  }

  @Test
  void resolveListReturnsEmptyList() {
    assertThat(new EmptyResolver().resolveList("anything"))
        .as("resolveList() should always return an empty list")
        .isEmpty();
  }

  @Test
  void resolveReturnsSelfAsNullObject() {
    EmptyResolver resolver = new EmptyResolver();

    assertThat(resolver.resolve("anything"))
        .as("resolve() should return the same EmptyResolver (null-object behavior)")
        .isSameAs(resolver);
  }

  @Test
  void valueReturnsEmptyResolvedValue() {
    assertThat(new EmptyResolver().value("anything"))
        .as("value() should always return ResolvedValue.EMPTY")
        .isSameAs(ResolvedValue.empty());
  }

  @Test
  void resolverEmptyConstantIsEmpty() {
    assertThat(Resolver.empty().isEmpty())
        .as("Resolver.empty() should be an empty resolver")
        .isTrue();
  }
}
