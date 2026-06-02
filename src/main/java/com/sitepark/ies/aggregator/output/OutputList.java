package com.sitepark.ies.aggregator.output;

import java.util.*;
import java.util.function.Predicate;

/**
 * Output node that holds an ordered list of {@link OutputListItem} elements.
 *
 * <p>Items can be added individually via {@link #addItem()} or located by predicate via
 * {@link #findFirst} and {@link #find}. The list enforces that only items whose parent is this
 * list may be added.
 */
public class OutputList implements Output {
  private final String field;
  private final Output parent;
  private final List<OutputListItem> items = new LinkedList<>();

  /**
   * @param field the field name under which this list is stored in its parent; must not be
   *     {@code null}
   * @param parent the parent node; must not be {@code null}
   */
  public OutputList(String field, Output parent) {
    this.field = Objects.requireNonNull(field);
    this.parent = Objects.requireNonNull(parent);
  }

  /**
   * Creates a new {@link OutputListItem}, appends it to this list, and returns it.
   *
   * @return the newly created item
   */
  public OutputListItem addItem() {
    OutputListItem item = new OutputListItem(this);
    this.addItem(item);
    return item;
  }

  /**
   * Adds an existing item to this list.
   *
   * @param item the item to add; its parent must be this list
   * @throws IllegalArgumentException if the item's parent is not this list
   */
  @SuppressWarnings("PMD.CompareObjectsWithEquals")
  public void addItem(OutputListItem item) {
    if (item.parent() != this) {
      throw new IllegalArgumentException("Parent of item must be this list");
    }
    items.add(item);
  }

  /**
   * Returns the first item matching the predicate, or {@code null} if none matches.
   *
   * @param fn the predicate to test each item against
   * @return the first matching item, or {@code null}
   */
  public OutputListItem findFirst(Predicate<OutputListItem> fn) {
    return items.stream().filter(fn).findFirst().orElse(null);
  }

  /**
   * Returns all items matching the predicate.
   *
   * @param fn the predicate to test each item against
   * @return the matching items, possibly empty
   */
  public List<OutputListItem> find(Predicate<OutputListItem> fn) {
    return items.stream().filter(fn).toList();
  }

  /** Returns an unmodifiable view of all items in insertion order. */
  public List<OutputListItem> items() {
    return Collections.unmodifiableList(items);
  }

  @Override
  public Output parent() {
    return parent;
  }

  @Override
  public OutputObject root() {
    return parent.root();
  }

  /**
   * Returns the zero-based index of {@code item} in this list, or {@code -1} if not found.
   *
   * @param item the item to locate
   */
  public int indexOf(OutputListItem item) {
    return items.indexOf(item);
  }

  @Override
  public String field() {
    return this.field;
  }

  @Override
  public String path() {
    return this.parent.path() + '.' + this.field;
  }

  @Override
  public void accept(OutputVisitor visitor) {
    visitor.visitList(this);
  }
}
