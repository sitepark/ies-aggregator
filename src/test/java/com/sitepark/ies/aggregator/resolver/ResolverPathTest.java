package com.sitepark.ies.aggregator.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.sitepark.ies.aggregator.resolver.ResolverPath.Segment;
import org.junit.jupiter.api.Test;

class ResolverPathTest {

  @Test
  void ofHasSingleSegmentWithNullKey() {
    Resolver root = mock();

    ResolverPath path = ResolverPath.of(root);

    assertThat(path.segments())
        .as("of() should create a single root segment with a null key")
        .containsExactly(new Segment(null, root));
  }

  @Test
  void ofIsItsOwnRootAndGlobalRoot() {
    Resolver root = mock();

    ResolverPath path = ResolverPath.of(root);

    assertThat(path.root()).as("of() root should be the resolver itself").isSameAs(root);
    assertThat(path.globalRoot())
        .as("of() globalRoot should be the resolver itself")
        .isSameAs(root);
  }

  @Test
  void createRootReturnsTheCreatedResolverAsItsOwnRoot() {
    Resolver root = mock();
    ResolverPath[] captured = new ResolverPath[1];

    Resolver created =
        ResolverPath.createRoot(
            path -> {
              captured[0] = path;
              return root;
            });

    assertThat(created)
        .as("createRoot() should return the resolver produced by the factory")
        .isSameAs(root);
    assertThat(captured[0].root())
        .as("The created root resolver should be its own scope root")
        .isSameAs(root);
    assertThat(captured[0].globalRoot())
        .as("The created root resolver should be its own global root")
        .isSameAs(root);
  }

  @Test
  void descendKeepsRootUnchangedAndAppendsTheChild() {
    Resolver root = mock();
    Resolver child = mock();
    ResolverPath childPath = descend(ResolverPath.of(root), "address", child);

    assertThat(childPath.root())
        .as("descend() should not change the current scope root")
        .isSameAs(root);
    assertThat(childPath.current().resolver())
        .as("The newest segment should hold the descended-into resolver")
        .isSameAs(child);
  }

  @Test
  void enterScopeMakesChildTheNewRootButKeepsGlobalRoot() {
    Resolver root = mock();
    Resolver author = mock();
    ResolverPath childPath = enterScope(ResolverPath.of(root), "author", author);

    assertThat(childPath.root())
        .as("enterScope() should make the linked object the new scope root")
        .isSameAs(author);
    assertThat(childPath.globalRoot())
        .as("enterScope() should not change the global root")
        .isSameAs(root);
  }

  @Test
  void enterRootMakesChildBothRootAndGlobalRoot() {
    Resolver root = mock();
    Resolver newRoot = mock();
    ResolverPath childPath = enterRoot(ResolverPath.of(root), newRoot);

    assertThat(childPath.root())
        .as("enterRoot() should make the created child the new scope root")
        .isSameAs(newRoot);
    assertThat(childPath.globalRoot())
        .as("enterRoot() should also make the created child the new global root")
        .isSameAs(newRoot);
  }

  @Test
  void enterRootAppendsSegmentWithNullKey() {
    Resolver root = mock();
    Resolver newRoot = mock();
    ResolverPath childPath = enterRoot(ResolverPath.of(root), newRoot);

    assertThat(childPath.size())
        .as("enterRoot() should keep the navigation history by appending a segment")
        .isEqualTo(2);
    assertThat(childPath.current())
        .as("The appended segment should carry the new root under a null key")
        .isEqualTo(new Segment(null, newRoot));
  }

  @Test
  void pathGrowsAcrossMultipleScopesWithStableGlobalRoot() {
    Resolver root = mock();
    Resolver author = mock();

    ResolverPath path =
        descend(
            enterScope(descend(ResolverPath.of(root), "address", mock()), "author", author),
            "name",
            mock());

    assertThat(path.size()).as("Path should grow by one segment per step").isEqualTo(4);
    assertThat(path.globalRoot())
        .as("Global root should remain stable across scope boundaries")
        .isSameAs(root);
    assertThat(path.root())
        .as("Root should reflect the most recently entered scope")
        .isSameAs(author);
  }

  @Test
  void keysReturnsAllSegmentKeysWithNullForRoot() {
    Resolver root = mock();

    ResolverPath path = descend(descend(ResolverPath.of(root), "a", mock()), "b", mock());

    assertThat(path.keys())
        .as("keys() should list every step's key with null for the root segment")
        .containsExactly(null, "a", "b");
  }

  @Test
  void descendDoesNotMutateTheOriginalPath() {
    ResolverPath original = ResolverPath.of(mock());

    descend(original, "a", mock());

    assertThat(original.size())
        .as("Extending a path should leave the original unchanged")
        .isEqualTo(1);
  }

  @Test
  void pathsWithEqualSegmentsAreEqual() {
    Resolver root = mock();

    assertThat(ResolverPath.of(root))
        .as("Paths anchored to the same resolver should be equal")
        .isEqualTo(ResolverPath.of(root));
    assertThat(ResolverPath.of(root).hashCode())
        .as("Equal paths should share a hash code")
        .isEqualTo(ResolverPath.of(root).hashCode());
  }

  @Test
  void pathsWithDifferentRootsAreNotEqual() {
    assertThat(ResolverPath.of(mock()))
        .as("Paths anchored to different resolver instances should not be equal")
        .isNotEqualTo(ResolverPath.of(mock()));
  }

  @Test
  void queryingThePathDuringConstructionThrows() {
    assertThatThrownBy(() -> ResolverPath.createRoot(ResolverPath::root))
        .as("A factory must not query its still-unbound path during construction")
        .isInstanceOf(IllegalStateException.class);
  }

  private static ResolverPath descend(ResolverPath parent, String key, Resolver child) {
    ResolverPath[] captured = new ResolverPath[1];
    parent.descend(
        key,
        path -> {
          captured[0] = path;
          return child;
        });
    return captured[0];
  }

  private static ResolverPath enterScope(ResolverPath parent, String key, Resolver child) {
    ResolverPath[] captured = new ResolverPath[1];
    parent.enterScope(
        key,
        path -> {
          captured[0] = path;
          return child;
        });
    return captured[0];
  }

  private static ResolverPath enterRoot(ResolverPath parent, Resolver child) {
    ResolverPath[] captured = new ResolverPath[1];
    parent.enterRoot(
        path -> {
          captured[0] = path;
          return child;
        });
    return captured[0];
  }
}
