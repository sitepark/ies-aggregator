package com.sitepark.ies.aggregator.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.sitepark.ies.aggregator.value.ResolvedValue;
import org.junit.jupiter.api.Test;

class EmptyEntityResolverTest {

  @Test
  void isAlwaysEmpty() {
    assertThat(EntityResolver.empty(pathOf(mock(), mock())).isEmpty())
        .as("EmptyEntityResolver should always report itself as empty")
        .isTrue();
  }

  @Test
  void entityIdIsZero() {
    assertThat(EntityResolver.empty(pathOf(mock(), mock())).entityId())
        .as("An empty entity should have id 0")
        .isZero();
  }

  @Test
  void entityTypeIsEmpty() {
    assertThat(EntityResolver.empty(pathOf(mock(), mock())).entityType())
        .as("An empty entity should have an empty type")
        .isEmpty();
  }

  @Test
  void entityNameIsEmpty() {
    assertThat(EntityResolver.empty(pathOf(mock(), mock())).entityName())
        .as("An empty entity should have an empty name")
        .isEmpty();
  }

  @Test
  void parentGroupIsNull() {
    assertThat(EntityResolver.empty(pathOf(mock(), mock())).parentGroup())
        .as("An empty entity should have no parent group")
        .isNull();
  }

  @Test
  void parentGroupPathIsEmpty() {
    assertThat(EntityResolver.empty(pathOf(mock(), mock())).parentGroupPath())
        .as("An empty entity should have an empty parent group path")
        .isEmpty();
  }

  @Test
  void resolveListReturnsEmptyList() {
    assertThat(EntityResolver.empty(pathOf(mock(), mock())).resolveList("anything"))
        .as("resolveList() should always return an empty list")
        .isEmpty();
  }

  @Test
  void resolveReturnsSelfAsNullObject() {
    EntityResolver resolver = EntityResolver.empty(pathOf(mock(), mock()));

    assertThat(resolver.resolve("anything"))
        .as("resolve() should return the same EmptyEntityResolver (null-object behavior)")
        .isSameAs(resolver);
  }

  @Test
  void valueReturnsEmptyResolvedValue() {
    assertThat(EntityResolver.empty(pathOf(mock(), mock())).value("anything"))
        .as("value() should always return ResolvedValue.EMPTY")
        .isSameAs(ResolvedValue.empty());
  }

  @Test
  void exposesSuppliedPath() {
    ResolverPath path = pathOf(mock(), mock());

    assertThat(EntityResolver.empty(path).path())
        .as("An empty entity resolver should expose the exact path supplied at construction")
        .isSameAs(path);
  }

  @Test
  void returnsSuppliedRoot() {
    Resolver globalRoot = mock();
    Resolver root = mock();

    assertThat(EntityResolver.empty(pathOf(globalRoot, root)).root())
        .as("An empty entity resolver should return the root supplied at construction")
        .isSameAs(root);
  }

  @Test
  void returnsSuppliedGlobalRoot() {
    Resolver globalRoot = mock();
    Resolver root = mock();

    assertThat(EntityResolver.empty(pathOf(globalRoot, root)).globalRoot())
        .as("An empty entity resolver should return the global root supplied at construction")
        .isSameAs(globalRoot);
  }

  @Test
  void resolversWithSameContextAreEqual() {
    Resolver globalRoot = mock();
    Resolver root = mock();

    assertThat(EntityResolver.empty(pathOf(globalRoot, root)))
        .as("Empty entity resolvers anchored to the same root and global root should be equal")
        .isEqualTo(EntityResolver.empty(pathOf(globalRoot, root)));
  }

  @Test
  void equalResolversShareHashCode() {
    Resolver globalRoot = mock();
    Resolver root = mock();

    assertThat(EntityResolver.empty(pathOf(globalRoot, root)).hashCode())
        .as("Equal empty entity resolvers should produce the same hash code")
        .isEqualTo(EntityResolver.empty(pathOf(globalRoot, root)).hashCode());
  }

  @Test
  void resolversAnchoredToDifferentRootsAreNotEqual() {
    Resolver globalRoot = mock();

    assertThat(EntityResolver.empty(pathOf(globalRoot, mock())))
        .as("Empty entity resolvers anchored to different roots should not be equal")
        .isNotEqualTo(EntityResolver.empty(pathOf(globalRoot, mock())));
  }

  @Test
  void emptyEntityResolverIsNotEqualToUnrelatedObject() {
    assertThat(EntityResolver.empty(pathOf(mock(), mock())))
        .as("An empty entity resolver should not equal an object of an unrelated type")
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
