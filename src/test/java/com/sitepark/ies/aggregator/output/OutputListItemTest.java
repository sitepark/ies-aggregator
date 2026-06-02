package com.sitepark.ies.aggregator.output;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OutputListItemTest {

  @Test
  void parentIsTheContainingList() {
    OutputObject root = new OutputObject(null, null);
    OutputList list = root.nodeList("items");
    OutputListItem item = list.addItem();

    assertThat(item.parent()).as("parent() should return the containing list").isSameAs(list);
  }

  @Test
  void fieldIncludesPositionWithinParentList() {
    OutputObject root = new OutputObject(null, null);
    OutputList list = root.nodeList("items");
    list.addItem();
    OutputListItem second = list.addItem();

    assertThat(second.field())
        .as("field() should be 'listField[index]' reflecting the position within the parent list")
        .isEqualTo("items[1]");
  }

  @Test
  void pathIncludesParentPathAndIndex() {
    OutputObject root = new OutputObject(null, null);
    OutputList list = root.node("a").nodeList("items");
    list.addItem();
    OutputListItem second = list.addItem();

    assertThat(second.path())
        .as("path() should reflect parent's path plus the zero-based index in brackets")
        .isEqualTo("a.items[1]");
  }

  @Test
  void rootDelegatesToParentList() {
    OutputObject root = new OutputObject(null, null);
    OutputListItem item = root.nodeList("items").addItem();

    assertThat(item.root())
        .as("root() should reach the actual root object via the parent list")
        .isSameAs(root);
  }
}
