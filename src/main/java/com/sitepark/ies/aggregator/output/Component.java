package com.sitepark.ies.aggregator.output;

import org.jspecify.annotations.Nullable;

/**
 * A component in the output: a list item that knows what it is and which one it is, and that may
 * carry components of its own.
 *
 * <p>Aggregation produces trees of typed, identified nodes — a page holds containers, a container
 * holds sections, a section may hold parts. This is the one shape all of them share: a {@code type}
 * that says what the consumer is looking at, an {@code id} that tells it apart from its siblings,
 * and its children listed under {@code items}. Nothing here decides what types exist or how ids are
 * given out; that is the business of whoever builds the tree.
 *
 * <p>A child can be created in two steps so that a caller may drop it again: {@link #newComponent}
 * hands out a component that is not yet part of its parent, {@link #addComponent(Component)} attaches
 * it once it turned out to carry content. {@link #addComponent(String, String)} does both at once.
 */
public class Component extends OutputListItem {

  private static final String TYPE = "type";
  private static final String ID = "id";
  private static final String ITEMS = "items";

  /**
   * @param parent the list this component is an item of
   * @param type what the component is, as the consumer names it
   * @param id what tells the component apart from its siblings
   */
  // put() is the node-population API of this package, and stamping type and id is the whole point
  // of this constructor; a subclass that overrides put() has to expect to see them.
  @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
  public Component(OutputList parent, String type, String id) {
    super(parent);
    this.put(TYPE, type);
    this.put(ID, id);
  }

  public @Nullable String id() {
    return this.getString(ID);
  }

  public @Nullable String type() {
    return this.getString(TYPE);
  }

  /**
   * Creates a child component and attaches it right away.
   *
   * @param type the component type
   * @param id the component id
   * @return the attached child
   */
  public Component addComponent(String type, String id) {
    OutputList items = this.nodeList(ITEMS);
    Component item = new Component(items, type, id);
    items.addItem(item);
    return item;
  }

  /**
   * Creates a child component without attaching it to this one yet.
   *
   * <p>The component has to be attached via {@link #addComponent(Component)} once populated. This
   * lets a caller discard a component that turned out to be empty.
   *
   * @param type the component type
   * @param id the component id
   * @return the newly created, not yet attached child
   */
  public Component newComponent(String type, String id) {
    return new Component(this.nodeList(ITEMS), type, id);
  }

  /**
   * Attaches a component previously created via {@link #newComponent(String, String)}.
   *
   * @param component the component to attach; must have been created by this component
   */
  public void addComponent(Component component) {
    this.nodeList(ITEMS).addItem(component);
  }

  /**
   * Returns the child with the given type and id, creating and attaching it if there is none.
   *
   * @param type the component type
   * @param id the component id
   * @return the existing or the newly attached child
   */
  public Component findOrAddComponent(String type, String id) {
    for (OutputListItem item : this.nodeList(ITEMS).items()) {
      if (item instanceof Component component
          && id.equals(component.id())
          && type.equals(component.type())) {
        return component;
      }
    }
    return this.addComponent(type, id);
  }
}
