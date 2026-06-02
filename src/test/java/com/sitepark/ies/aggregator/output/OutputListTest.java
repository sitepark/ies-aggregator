package com.sitepark.ies.aggregator.output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class OutputListTest {

  @Test
  void constructorRejectsNullField() {
    OutputObject parent = new OutputObject(null, null);

    assertThatThrownBy(() -> new OutputList(null, parent))
        .as("Constructor should reject null field")
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void constructorRejectsNullParent() {
    assertThatThrownBy(() -> new OutputList("items", null))
        .as("Constructor should reject null parent")
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void addItemAppendsAndReturnsNewItem() {
    OutputObject parent = new OutputObject(null, null);
    OutputList list = parent.nodeList("items");

    OutputListItem item = list.addItem();

    assertThat(list.items())
        .as("addItem() should append the newly created item")
        .containsExactly(item);
    assertThat(item.parent())
        .as("Returned item should have this list as its parent")
        .isSameAs(list);
  }

  @Test
  void addItemRejectsItemFromDifferentParent() {
    OutputObject parent = new OutputObject(null, null);
    OutputList listA = parent.nodeList("a");
    OutputList listB = parent.nodeList("b");
    OutputListItem foreign = listA.addItem();

    assertThatThrownBy(() -> listB.addItem(foreign))
        .as("addItem(item) should reject items whose parent is a different list")
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void fieldReturnsConfiguredFieldName() {
    OutputObject parent = new OutputObject(null, null);
    OutputList list = parent.nodeList("items");

    assertThat(list.field())
        .as("field() should return the configured field name")
        .isEqualTo("items");
  }

  @Test
  void pathConcatenatesParentPathAndField() {
    OutputObject root = new OutputObject(null, null);
    OutputList list = root.node("a").nodeList("items");

    assertThat(list.path())
        .as("path() should concatenate parent path and field with a dot")
        .isEqualTo("a.items");
  }

  @Test
  void rootDelegatesToParent() {
    OutputObject root = new OutputObject(null, null);
    OutputList list = root.node("a").nodeList("items");

    assertThat(list.root())
        .as("root() should walk up via parent to the root object")
        .isSameAs(root);
  }

  @Test
  void indexOfReturnsZeroBasedPosition() {
    OutputObject parent = new OutputObject(null, null);
    OutputList list = parent.nodeList("items");
    OutputListItem first = list.addItem();
    OutputListItem second = list.addItem();

    assertThat(list.indexOf(first)).as("indexOf() should return 0 for the first item").isZero();
    assertThat(list.indexOf(second))
        .as("indexOf() should return 1 for the second item")
        .isEqualTo(1);
  }

  @Test
  void indexOfReturnsMinusOneForForeignItem() {
    OutputObject parent = new OutputObject(null, null);
    OutputList list = parent.nodeList("items");
    OutputListItem foreign = parent.nodeList("others").addItem();

    assertThat(list.indexOf(foreign))
        .as("indexOf() should return -1 for items not in this list")
        .isEqualTo(-1);
  }

  @Test
  void itemsIsUnmodifiable() {
    OutputObject parent = new OutputObject(null, null);
    OutputList list = parent.nodeList("items");
    list.addItem();

    assertThatThrownBy(() -> list.items().add(new OutputListItem(list)))
        .as("items() should return an unmodifiable view")
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void findFirstReturnsFirstMatchingItem() {
    OutputObject parent = new OutputObject(null, null);
    OutputList list = parent.nodeList("items");
    list.addItem().put("id", 1);
    OutputListItem second = list.addItem();
    second.put("id", 2);
    list.addItem().put("id", 2);

    assertThat(list.findFirst(item -> Integer.valueOf(2).equals(item.get("id"))))
        .as("findFirst() should return the first item matching the predicate")
        .isSameAs(second);
  }

  @Test
  void findFirstReturnsNullWhenNoMatch() {
    OutputObject parent = new OutputObject(null, null);
    OutputList list = parent.nodeList("items");
    list.addItem().put("id", 1);

    assertThat(list.findFirst(item -> Integer.valueOf(99).equals(item.get("id"))))
        .as("findFirst() should return null when no item matches the predicate")
        .isNull();
  }

  @Test
  void findReturnsAllMatchingItemsInOrder() {
    OutputObject parent = new OutputObject(null, null);
    OutputList list = parent.nodeList("items");
    OutputListItem first = list.addItem();
    first.put("group", "a");
    list.addItem().put("group", "b");
    OutputListItem third = list.addItem();
    third.put("group", "a");

    assertThat(list.find(item -> "a".equals(item.get("group"))))
        .as("find() should return all items matching the predicate, in original order")
        .containsExactly(first, third);
  }
}
