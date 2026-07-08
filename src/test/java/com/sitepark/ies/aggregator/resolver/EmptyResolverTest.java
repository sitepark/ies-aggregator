package com.sitepark.ies.aggregator.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.sitepark.ies.aggregator.value.ResolvedValue;
import org.junit.jupiter.api.Test;

class EmptyResolverTest {

  @Test
  void isAlwaysEmpty() {
    assertThat(Resolver.empty().isEmpty())
        .as("EmptyResolver should always report itself as empty")
        .isTrue();
  }

  @Test
  void resolveListReturnsEmptyList() {
    assertThat(Resolver.empty().resolveList("anything"))
        .as("resolveList() should always return an empty list")
        .isEmpty();
  }

  @Test
  void resolveReturnsSelfAsNullObject() {
    Resolver resolver = Resolver.empty();

    assertThat(resolver.resolve("anything"))
        .as("resolve() should return the same EmptyResolver (null-object behavior)")
        .isSameAs(resolver);
  }

  @Test
  void valueReturnsEmptyResolvedValue() {
    assertThat(Resolver.empty().value("anything"))
        .as("value() should always return ResolvedValue.EMPTY")
        .isSameAs(ResolvedValue.empty());
  }

  @Test
  void resolverEmptyConstantIsEmpty() {
    assertThat(Resolver.empty().isEmpty())
        .as("Resolver.empty() should be an empty resolver")
        .isTrue();
  }

  @Test
  void selfContainedResolverIsItsOwnRoot() {
    Resolver resolver = Resolver.empty();

    assertThat(resolver.root())
        .as("A self-contained empty resolver should be its own scope root")
        .isSameAs(resolver);
  }

  @Test
  void selfContainedResolverIsItsOwnGlobalRoot() {
    Resolver resolver = Resolver.empty();

    assertThat(resolver.globalRoot())
        .as("A self-contained empty resolver should be its own global root")
        .isSameAs(resolver);
  }

  @Test
  void contextAwareResolverExposesSuppliedPath() {
    ResolverPath path = pathOf(mock(), mock());

    assertThat(Resolver.empty(path).path())
        .as("A context-aware empty resolver should expose the exact path supplied at construction")
        .isSameAs(path);
  }

  @Test
  void contextAwareResolverReturnsSuppliedRoot() {
    Resolver globalRoot = mock();
    Resolver root = mock();

    assertThat(Resolver.empty(pathOf(globalRoot, root)).root())
        .as("A context-aware empty resolver should return the root supplied at construction")
        .isSameAs(root);
  }

  @Test
  void contextAwareResolverReturnsSuppliedGlobalRoot() {
    Resolver globalRoot = mock();
    Resolver root = mock();

    assertThat(Resolver.empty(pathOf(globalRoot, root)).globalRoot())
        .as("A context-aware empty resolver should return the global root supplied at construction")
        .isSameAs(globalRoot);
  }

  @Test
  void resolversWithSameContextAreEqual() {
    Resolver globalRoot = mock();
    Resolver root = mock();

    assertThat(Resolver.empty(pathOf(globalRoot, root)))
        .as("Empty resolvers anchored to the same root and global root should be equal")
        .isEqualTo(Resolver.empty(pathOf(globalRoot, root)));
  }

  @Test
  void equalResolversShareHashCode() {
    Resolver globalRoot = mock();
    Resolver root = mock();

    assertThat(Resolver.empty(pathOf(globalRoot, root)).hashCode())
        .as("Equal empty resolvers should produce the same hash code")
        .isEqualTo(Resolver.empty(pathOf(globalRoot, root)).hashCode());
  }

  @Test
  void resolversAnchoredToDifferentRootsAreNotEqual() {
    Resolver globalRoot = mock();

    assertThat(Resolver.empty(pathOf(globalRoot, mock())))
        .as("Empty resolvers anchored to different roots should not be equal")
        .isNotEqualTo(Resolver.empty(pathOf(globalRoot, mock())));
  }

  @Test
  void emptyResolverIsNotEqualToUnrelatedObject() {
    assertThat(Resolver.empty())
        .as("An empty resolver should not equal an object of an unrelated type")
        .isNotEqualTo("not a resolver");
  }

  /**
   * Builds a two-level path whose {@link ResolverPath#globalRoot()} is {@code globalRoot} and whose
   * {@link ResolverPath#root()} is {@code root}, by entering a scope and capturing the resulting
   * path.
   */
  private static ResolverPath pathOf(Resolver globalRoot, Resolver root) {
    ResolverPath[] captured = new ResolverPath[1];
    ResolverPath.of(globalRoot)
        .enterScope(
            "scope",
            path -> {
              captured[0] = path;
              return root;
            });
    return captured[0];
  }
}
