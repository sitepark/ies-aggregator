package com.sitepark.ies.aggregator.output;

import com.sitepark.ies.aggregator.value.uri.Uri;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Abstract base class for mutable output nodes that store named fields.
 *
 * <p>A node holds an ordered map of field name → value pairs. Values can be scalars (strings,
 * numbers, booleans), typed value objects ({@link
 * com.sitepark.ies.aggregator.value.text.TranslatableText TranslatableText}, {@link Uri Uri}, …), nested
 * {@link OutputObject} nodes, or {@link OutputList} nodes.
 *
 * <p>Sub-nodes are created lazily: {@link #node(String)} and {@link #nodeList(String)} return an
 * existing sub-node or create a new one.
 */
public abstract class OutputNode implements Output {

  private final @Nullable Output parent;
  private final Map<String, Object> content = new LinkedHashMap<>();

  /**
   * @param parent the parent node, or {@code null} for the root
   */
  public OutputNode(@Nullable Output parent) {
    this.parent = parent;
  }

  /**
   * Stores {@code value} under {@code field}, replacing any previous value.
   *
   * @param field the field name
   * @param value the value to store
   */
  public void put(String field, Object value) {
    this.content.put(field, value);
  }

  /**
   * Returns {@code true} if a value is stored under {@code field}.
   *
   * @param field the field name
   */
  public boolean has(String field) {
    return this.content.containsKey(field);
  }

  /**
   * Returns the value stored under {@code field}, or {@code null} if absent.
   *
   * @param field the field name
   */
  public @Nullable Object get(String field) {
    return this.content.get(field);
  }

  /**
   * Returns the string representation of the value stored under {@code field}, or {@code null}.
   *
   * @param field the field name
   */
  public @Nullable String getString(String field) {
    Object o = this.content.get(field);
    if (o == null) {
      return null;
    }
    return o.toString();
  }

  /**
   * Copies all entries from {@code map} into this node's field map.
   *
   * @param map the entries to add
   */
  public void putAll(Map<String, Object> map) {
    this.content.putAll(map);
  }

  /**
   * Returns the existing child {@link OutputObject} for {@code field}, or creates and stores a new
   * one.
   *
   * @param field the field name
   * @return the existing or newly created child object
   */
  public OutputObject node(String field) {
    Object value = this.content.get(field);
    if (value instanceof OutputObject contentValue) {
      return contentValue;
    }

    OutputObject content = new OutputObject(field, this);
    this.content.put(field, content);
    return content;
  }

  /**
   * Returns the existing child {@link OutputList} for {@code field}, or creates and stores a new
   * one.
   *
   * @param field the field name
   * @return the existing or newly created child list
   */
  public OutputList nodeList(String field) {
    Object value = this.content.get(field);
    if (value instanceof OutputList contentValue) {
      return contentValue;
    }

    OutputList list = new OutputList(field, this);
    this.content.put(field, list);
    return list;
  }

  /**
   * Returns the existing child node for {@code field}; if the node is new, populates it with {@code
   * defaults} first.
   *
   * @param field the field name
   * @param defaults default values applied on first creation
   * @return the child node
   */
  public OutputObject resolveOrInitNode(String field, Map<String, Object> defaults) {
    boolean isNew = !has(field);
    OutputObject child = node(field);
    if (isNew) {
      child.putAll(defaults);
    }
    return child;
  }

  /** Returns an unmodifiable view of all entries in document order. */
  public Map<String, Object> entries() {
    return Collections.unmodifiableMap(this.content);
  }

  @Override
  public @Nullable Output parent() {
    return this.parent;
  }

  /** Returns the root {@link OutputObject} of the entire tree. */
  @Override
  public abstract OutputObject root();

  /** Returns the dot-separated path from the root to this node. */
  @Override
  public abstract String path();
}
