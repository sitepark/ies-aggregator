package com.sitepark.ies.aggregator.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.sitepark.ies.aggregator.value.ResolvedValue;
import org.junit.jupiter.api.Test;

class EmptyGroupResolverTest {

  @Test
  void isAlwaysEmpty() {
    assertThat(GroupResolver.empty(pathOf(mock(), mock())).isEmpty())
        .as("EmptyGroupResolver should always report itself as empty")
        .isTrue();
  }

  @Test
  void entityIdIsZero() {
    assertThat(GroupResolver.empty(pathOf(mock(), mock())).entityId())
        .as("An empty group should have id 0")
        .isZero();
  }

  @Test
  void entityTypeIsEmpty() {
    assertThat(GroupResolver.empty(pathOf(mock(), mock())).entityType())
        .as("An empty group should have an empty type")
        .isEmpty();
  }

  @Test
  void entityNameIsEmpty() {
    assertThat(GroupResolver.empty(pathOf(mock(), mock())).entityName())
        .as("An empty group should have an empty name")
        .isEmpty();
  }

  @Test
  void entityAnchorIsEmpty() {
    assertThat(GroupResolver.empty(pathOf(mock(), mock())).entityAnchor())
        .as("An empty group should have an empty anchor")
        .isEmpty();
  }

  @Test
  void parentGroupIsNull() {
    assertThat(GroupResolver.empty(pathOf(mock(), mock())).parentGroup())
        .as("An empty group should have no parent group")
        .isNull();
  }

  @Test
  void parentGroupPathIsEmpty() {
    assertThat(GroupResolver.empty(pathOf(mock(), mock())).parentGroupPath())
        .as("An empty group should have an empty parent group path")
        .isEmpty();
  }

  @Test
  void groupSubGroupsIsEmpty() {
    assertThat(GroupResolver.empty(pathOf(mock(), mock())).groupSubGroups())
        .as("An empty group should have no sub-groups")
        .isEmpty();
  }

  @Test
  void groupEntitiesIsEmpty() {
    assertThat(GroupResolver.empty(pathOf(mock(), mock())).groupEntities())
        .as("An empty group should contain no entities")
        .isEmpty();
  }

  @Test
  void groupChildrenIsEmpty() {
    assertThat(GroupResolver.empty(pathOf(mock(), mock())).groupChildren())
        .as("An empty group should have no children")
        .isEmpty();
  }

  @Test
  void isNotARootSiteGroup() {
    assertThat(GroupResolver.empty(pathOf(mock(), mock())).isRootSiteGroup())
        .as("An empty group should not be a site root group")
        .isFalse();
  }

  @Test
  void isNotAMicrositeRootSiteGroup() {
    assertThat(GroupResolver.empty(pathOf(mock(), mock())).isMicrositeRootSiteGroup())
        .as("An empty group should not be a microsite root group")
        .isFalse();
  }

  @Test
  void langIsEmpty() {
    assertThat(GroupResolver.empty(pathOf(mock(), mock())).lang())
        .as("An empty group should have an empty language")
        .isEmpty();
  }

  @Test
  void resolveListReturnsEmptyList() {
    assertThat(GroupResolver.empty(pathOf(mock(), mock())).resolveList("anything"))
        .as("resolveList() should always return an empty list")
        .isEmpty();
  }

  @Test
  void resolveReturnsSelfAsNullObject() {
    GroupResolver resolver = GroupResolver.empty(pathOf(mock(), mock()));

    assertThat(resolver.resolve("anything"))
        .as("resolve() should return the same EmptyGroupResolver (null-object behavior)")
        .isSameAs(resolver);
  }

  @Test
  void valueReturnsEmptyResolvedValue() {
    assertThat(GroupResolver.empty(pathOf(mock(), mock())).value("anything"))
        .as("value() should always return ResolvedValue.EMPTY")
        .isSameAs(ResolvedValue.empty());
  }

  @Test
  void exposesSuppliedPath() {
    ResolverPath path = pathOf(mock(), mock());

    assertThat(GroupResolver.empty(path).path())
        .as("An empty group resolver should expose the exact path supplied at construction")
        .isSameAs(path);
  }

  @Test
  void returnsSuppliedRoot() {
    Resolver globalRoot = mock();
    Resolver root = mock();

    assertThat(GroupResolver.empty(pathOf(globalRoot, root)).root())
        .as("An empty group resolver should return the root supplied at construction")
        .isSameAs(root);
  }

  @Test
  void returnsSuppliedGlobalRoot() {
    Resolver globalRoot = mock();
    Resolver root = mock();

    assertThat(GroupResolver.empty(pathOf(globalRoot, root)).globalRoot())
        .as("An empty group resolver should return the global root supplied at construction")
        .isSameAs(globalRoot);
  }

  @Test
  void resolversWithSameContextAreEqual() {
    Resolver globalRoot = mock();
    Resolver root = mock();

    assertThat(GroupResolver.empty(pathOf(globalRoot, root)))
        .as("Empty group resolvers anchored to the same root and global root should be equal")
        .isEqualTo(GroupResolver.empty(pathOf(globalRoot, root)));
  }

  @Test
  void equalResolversShareHashCode() {
    Resolver globalRoot = mock();
    Resolver root = mock();

    assertThat(GroupResolver.empty(pathOf(globalRoot, root)).hashCode())
        .as("Equal empty group resolvers should produce the same hash code")
        .isEqualTo(GroupResolver.empty(pathOf(globalRoot, root)).hashCode());
  }

  @Test
  void resolversAnchoredToDifferentRootsAreNotEqual() {
    Resolver globalRoot = mock();

    assertThat(GroupResolver.empty(pathOf(globalRoot, mock())))
        .as("Empty group resolvers anchored to different roots should not be equal")
        .isNotEqualTo(GroupResolver.empty(pathOf(globalRoot, mock())));
  }

  @Test
  void emptyGroupResolverIsNotEqualToUnrelatedObject() {
    assertThat(GroupResolver.empty(pathOf(mock(), mock())))
        .as("An empty group resolver should not equal an object of an unrelated type")
        .isNotEqualTo("not a resolver");
  }

  @Test
  void emptyGroupResolverIsNotEqualToEmptyEntityResolver() {
    Resolver globalRoot = mock();
    Resolver root = mock();

    assertThat(GroupResolver.empty(pathOf(globalRoot, root)))
        .as("An empty group resolver should not equal an empty entity resolver")
        .isNotEqualTo(EntityResolver.empty(pathOf(globalRoot, root)));
  }

  @Test
  void emptyRootIsEmpty() {
    assertThat(GroupResolver.emptyRoot().isEmpty())
        .as("A self-rooted empty group resolver should report itself as empty")
        .isTrue();
  }

  @Test
  void emptyRootIsItsOwnRootAndGlobalRoot() {
    GroupResolver resolver = GroupResolver.emptyRoot();

    assertThat(resolver.root())
        .as("A self-rooted empty group resolver should be its own root")
        .isSameAs(resolver);
    assertThat(resolver.globalRoot())
        .as("A self-rooted empty group resolver should be its own global root")
        .isSameAs(resolver);
  }

  @Test
  void emptyRootStartsAFreshPath() {
    GroupResolver resolver = GroupResolver.emptyRoot();

    assertThat(resolver.path().size())
        .as("The path of a self-rooted empty group resolver should consist of a single segment")
        .isOne();
    assertThat(resolver.path().current().resolver())
        .as("The only path segment should point back at the resolver itself")
        .isSameAs(resolver);
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
