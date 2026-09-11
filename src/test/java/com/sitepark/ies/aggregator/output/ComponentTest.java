package com.sitepark.ies.aggregator.output;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ComponentTest {

  private static Component aComponent() {
    OutputObject root = new OutputObject(null, null);
    OutputList list = root.nodeList("items");
    Component component = new Component(list, "container", "main");
    list.addItem(component);
    return component;
  }

  @Test
  void carriesItsTypeAndIdAsFields() {
    Component component = aComponent();

    assertThat(component.type()).as("the type is stored as a field").isEqualTo("container");
    assertThat(component.id()).as("the id is stored as a field").isEqualTo("main");
    assertThat(component.getString("type"))
        .as("the type is readable as an ordinary output field, that is what the consumer sees")
        .isEqualTo("container");
  }

  @Test
  void addComponentAttachesTheChildUnderItems() {
    Component container = aComponent();

    Component child = container.addComponent("text", "text-1");

    assertThat(container.nodeList("items").items())
        .as("the child is listed under items right away")
        .containsExactly(child);
    assertThat(child.parent())
        .as("the child sits in the container's items list")
        .isSameAs(container.nodeList("items"));
  }

  @Test
  void newComponentIsNotAttachedUntilAdded() {
    Component container = aComponent();

    Component child = container.newComponent("text", "text-1");

    assertThat(container.nodeList("items").items())
        .as("a component created in two steps is not part of its parent yet")
        .isEmpty();

    container.addComponent(child);

    assertThat(container.nodeList("items").items())
        .as("once attached it is listed like any other child")
        .containsExactly(child);
  }

  @Test
  void findOrAddComponentAnswersTheExistingChild() {
    Component container = aComponent();
    Component existing = container.addComponent("text", "text-1");

    Component found = container.findOrAddComponent("text", "text-1");

    assertThat(found)
        .as("a child with the same type and id is not created twice")
        .isSameAs(existing);
    assertThat(container.nodeList("items").items()).as("no second child appeared").hasSize(1);
  }

  @Test
  void findOrAddComponentTellsChildrenApartByTypeAndId() {
    Component container = aComponent();
    container.addComponent("text", "text-1");

    Component other = container.findOrAddComponent("image", "text-1");

    assertThat(other.type())
        .as("the same id under a different type is a different child")
        .isEqualTo("image");
    assertThat(container.nodeList("items").items()).as("both children are listed").hasSize(2);
  }
}
