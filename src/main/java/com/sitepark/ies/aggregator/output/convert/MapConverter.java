package com.sitepark.ies.aggregator.output.convert;

import com.sitepark.ies.aggregator.output.DomainObjectMapper;
import com.sitepark.ies.aggregator.output.OutputList;
import com.sitepark.ies.aggregator.output.OutputListItem;
import com.sitepark.ies.aggregator.output.OutputObject;
import com.sitepark.ies.aggregator.output.OutputVisitor;
import com.sitepark.ies.aggregator.output.format.RawPhpCode;
import com.sitepark.ies.aggregator.value.PlainText;
import com.sitepark.ies.aggregator.value.PlainUri;
import com.sitepark.ies.aggregator.value.ResolvedValue;
import com.sitepark.ies.aggregator.value.TranslatableSplitText;
import com.sitepark.ies.aggregator.value.TranslatableText;
import com.sitepark.ies.aggregator.value.TranslatableUri;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Visitor that converts an {@link OutputObject} or {@link OutputList} hierarchy into nested {@link
 * Map} / {@link List} structures.
 *
 * <p>Scalar value classes ({@link PlainText}, {@link TranslatableText}, {@link PlainUri}, {@link
 * TranslatableUri}, {@link TranslatableSplitText}, {@link RawPhpCode}) are kept as-is — their typed
 * identity is preserved rather than stringified. {@link ResolvedValue} is unwrapped to its inner
 * value, so the result of {@code resolvedValue.value()} appears in the map.
 */
public final class MapConverter extends OutputVisitor {

  private final Deque<Container> stack = new ArrayDeque<>();
  private String currentKey;
  private Object result;

  /** Creates a converter without a domain object mapper. */
  public MapConverter() {
    super();
  }

  /**
   * Creates a converter with the given domain object mapper.
   *
   * @param domainObjectMapper the mapper for unwrapping domain objects
   */
  public MapConverter(DomainObjectMapper domainObjectMapper) {
    super(domainObjectMapper);
  }

  /**
   * Converts {@code root} to a {@code Map<String, Object>}.
   *
   * @param root the object to convert
   * @return the converted map
   */
  public Map<String, Object> toMap(OutputObject root) {
    reset();
    root.accept(this);
    @SuppressWarnings("unchecked")
    Map<String, Object> typed = (Map<String, Object>) this.result;
    return typed;
  }

  /**
   * Converts {@code root} to a {@code List<Map<String, Object>>}.
   *
   * @param root the list to convert
   * @return the converted list of maps
   */
  public List<Map<String, Object>> toList(OutputList root) {
    reset();
    root.accept(this);
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> typed = (List<Map<String, Object>>) this.result;
    return typed;
  }

  /**
   * Converts an arbitrary {@link Collection} to a {@code List<Object>}, preserving typed values.
   *
   * @param root the collection to convert
   * @return the converted list
   */
  public List<Object> toRawList(Collection<?> root) {
    reset();
    visitCollection(root);
    @SuppressWarnings("unchecked")
    List<Object> typed = (List<Object>) this.result;
    return typed;
  }

  private void reset() {
    this.stack.clear();
    this.currentKey = null;
    this.result = null;
  }

  @Override
  public void visitObject(OutputObject obj) {
    Map<String, Object> map = new LinkedHashMap<>();
    putValue(map);
    this.stack.push(new MapContainer(map));
    iterateFields(obj);
    this.stack.pop();
  }

  @Override
  public void visitList(OutputList list) {
    List<Object> array = new ArrayList<>();
    putValue(array);
    this.stack.push(new ListContainer(array));
    for (OutputListItem item : list.items()) {
      visitListItem(item);
    }
    this.stack.pop();
  }

  @Override
  public void visitCollection(Collection<?> collection) {
    List<Object> nested = new ArrayList<>();
    putValue(nested);
    this.stack.push(new ListContainer(nested));
    for (Object item : collection) {
      visitField(null, item);
    }
    this.stack.pop();
  }

  @Override
  public void visitArray(Object[] array) {
    List<Object> nested = new ArrayList<>();
    putValue(nested);
    this.stack.push(new ListContainer(nested));
    for (Object item : array) {
      visitField(null, item);
    }
    this.stack.pop();
  }

  @Override
  public void visitListItem(OutputListItem item) {
    Map<String, Object> map = new LinkedHashMap<>();
    putValue(map);
    this.stack.push(new MapContainer(map));
    iterateFields(item);
    this.stack.pop();
  }

  @Override
  public void visitMap(Map<?, ?> map) {
    Map<String, Object> nested = new LinkedHashMap<>();
    putValue(nested);
    this.stack.push(new MapContainer(nested));
    map.forEach((k, v) -> visitField(k == null ? null : k.toString(), v));
    this.stack.pop();
  }

  @Override
  public void visitField(String key, Object value) {
    this.currentKey = key;
    super.visitField(key, value);
  }

  @Override
  public void visitString(String value) {
    putValue(value);
  }

  @Override
  public void visitNumber(Number value) {
    putValue(value);
  }

  @Override
  public void visitBoolean(Boolean value) {
    putValue(value);
  }

  @Override
  public void visitNull() {
    putValue(null);
  }

  @Override
  public void visitPlainText(PlainText value) {
    putValue(value);
  }

  @Override
  public void visitTranslatableText(TranslatableText value) {
    putValue(value);
  }

  @Override
  public void visitPlainUri(PlainUri value) {
    putValue(value);
  }

  @Override
  public void visitTranslatableUri(TranslatableUri value) {
    putValue(value);
  }

  @Override
  public void visitTranslatableSplitText(TranslatableSplitText value) {
    putValue(value);
  }

  @Override
  public void visitRawPhpCode(RawPhpCode value) {
    putValue(value);
  }

  @Override
  public void visitResolvedValue(ResolvedValue value) {
    super.visitField(this.currentKey, value.value());
  }

  @Override
  public void visitUnknown(Object value) {
    putValue(value);
  }

  private void putValue(Object value) {
    Container top = this.stack.peek();
    if (top == null) {
      this.result = value;
    } else {
      top.put(this.currentKey, value);
    }
  }

  private sealed interface Container permits MapContainer, ListContainer {
    void put(String key, Object value);
  }

  private record MapContainer(Map<String, Object> map) implements Container {
    @Override
    public void put(String key, Object value) {
      this.map.put(key, value);
    }
  }

  private record ListContainer(List<Object> list) implements Container {
    @Override
    public void put(String key, Object value) {
      this.list.add(value);
    }
  }
}
