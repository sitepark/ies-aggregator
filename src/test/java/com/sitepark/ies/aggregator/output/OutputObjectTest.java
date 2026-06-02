package com.sitepark.ies.aggregator.output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OutputObjectTest {

  @Test
  void rootHasNullFieldAndNullParent() {
    OutputObject root = new OutputObject(null, null);

    assertThat(root.field()).as("Root field should be null").isNull();
    assertThat(root.parent()).as("Root parent should be null").isNull();
    assertThat(root.isRoot()).as("Root should report isRoot() == true").isTrue();
  }

  @Test
  void rootPathIsEmptyString() {
    assertThat(new OutputObject(null, null).path())
        .as("Root path should be the empty string")
        .isEmpty();
  }

  @Test
  void rootReturnsSelfWhenNoParent() {
    OutputObject root = new OutputObject(null, null);

    assertThat(root.root())
        .as("root() should return self when this node has no parent")
        .isSameAs(root);
  }

  @Test
  void nodeReturnsExistingChildOnRepeatedAccess() {
    OutputObject root = new OutputObject(null, null);

    OutputObject first = root.node("meta");
    OutputObject second = root.node("meta");

    assertThat(second)
        .as("node(field) should return the same instance on repeated calls (lazy creation)")
        .isSameAs(first);
  }

  @Test
  void nodeAttachesChildAsParent() {
    OutputObject root = new OutputObject(null, null);

    OutputObject child = root.node("meta");

    assertThat(child.parent())
        .as("Newly created child should report the creating node as its parent")
        .isSameAs(root);
  }

  @Test
  void nestedNodesProduceDotSeparatedPath() {
    OutputObject root = new OutputObject(null, null);
    OutputObject grandchild = root.node("a").node("b");

    assertThat(grandchild.path())
        .as("Nested object path should be dot-separated from root")
        .isEqualTo("a.b");
  }

  @Test
  void nodeListReturnsExistingListOnRepeatedAccess() {
    OutputObject root = new OutputObject(null, null);

    OutputList first = root.nodeList("items");
    OutputList second = root.nodeList("items");

    assertThat(second)
        .as("nodeList(field) should return the same instance on repeated calls")
        .isSameAs(first);
  }

  @Test
  void getReturnsStoredValue() {
    OutputObject root = new OutputObject(null, null);
    root.put("name", "Alice");

    assertThat(root.get("name")).as("get() should return the stored value").isEqualTo("Alice");
  }

  @Test
  void getReturnsNullForUnknownField() {
    assertThat(new OutputObject(null, null).get("missing"))
        .as("get() should return null when no value was stored")
        .isNull();
  }

  @Test
  void hasReportsPresenceOfStoredField() {
    OutputObject root = new OutputObject(null, null);
    root.put("name", "Alice");

    assertThat(root.has("name")).as("has() should be true for a stored field").isTrue();
    assertThat(root.has("other"))
        .as("has() should be false for a field that was never set")
        .isFalse();
  }

  @Test
  void putReplacesExistingValue() {
    OutputObject root = new OutputObject(null, null);
    root.put("name", "Alice");
    root.put("name", "Bob");

    assertThat(root.get("name"))
        .as("put() should overwrite any existing value for the same field")
        .isEqualTo("Bob");
  }

  @Test
  void getStringReturnsToStringOfStoredValue() {
    OutputObject root = new OutputObject(null, null);
    root.put("count", 7);

    assertThat(root.getString("count"))
        .as("getString() should return toString() of the stored value")
        .isEqualTo("7");
  }

  @Test
  void getStringReturnsNullForUnknownField() {
    assertThat(new OutputObject(null, null).getString("missing"))
        .as("getString() should return null when no value was stored")
        .isNull();
  }

  @Test
  void putAllAddsAllEntries() {
    OutputObject root = new OutputObject(null, null);
    Map<String, Object> defaults = new LinkedHashMap<>();
    defaults.put("a", 1);
    defaults.put("b", 2);

    root.putAll(defaults);

    assertThat(root.entries())
        .as("putAll() should copy all map entries into the node")
        .containsAllEntriesOf(defaults);
  }

  @Test
  void resolveOrInitNodeAppliesDefaultsOnlyOnFirstCreation() {
    OutputObject root = new OutputObject(null, null);
    OutputObject first = root.resolveOrInitNode("meta", Map.of("type", "default"));
    first.put("type", "custom");

    OutputObject second = root.resolveOrInitNode("meta", Map.of("type", "would-overwrite"));

    assertThat(second)
        .as("resolveOrInitNode() should return the same instance on repeated calls")
        .isSameAs(first);
    assertThat(second.get("type"))
        .as("Defaults must not overwrite values written after the first creation")
        .isEqualTo("custom");
  }

  @Test
  void entriesPreserveInsertionOrder() {
    OutputObject root = new OutputObject(null, null);
    root.put("first", 1);
    root.put("second", 2);
    root.put("third", 3);

    assertThat(List.copyOf(root.entries().keySet()))
        .as("entries() should preserve insertion order")
        .containsExactly("first", "second", "third");
  }

  @Test
  void entriesIsUnmodifiable() {
    OutputObject root = new OutputObject(null, null);
    root.put("name", "Alice");
    Map<String, Object> entries = root.entries();

    assertThatThrownBy(() -> entries.put("foo", "bar"))
        .as("entries() should return an unmodifiable view")
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
