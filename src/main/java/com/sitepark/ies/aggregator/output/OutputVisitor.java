package com.sitepark.ies.aggregator.output;

import com.sitepark.ies.aggregator.output.format.RawPhpCode;
import com.sitepark.ies.aggregator.value.Emptiable;
import com.sitepark.ies.aggregator.value.ResolvedValue;
import com.sitepark.ies.aggregator.value.text.PlainText;
import com.sitepark.ies.aggregator.value.text.TranslatableSplitText;
import com.sitepark.ies.aggregator.value.text.TranslatableText;
import com.sitepark.ies.aggregator.value.text.Translations;
import com.sitepark.ies.aggregator.value.uri.PlainUri;
import com.sitepark.ies.aggregator.value.uri.TranslatableUri;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Visitor that traverses an {@link Output} tree.
 *
 * <p>Concrete subclasses implement specific output formats (PHP array, JSON, ...) or analyses (e.g.
 * collecting all {@link TranslatableText} instances). The visitor uses double dispatch: {@code
 * accept(visitor)} on a node calls the matching {@code visit*} method.
 *
 * <p>{@link #visitField(String, Object)} is the central dispatcher for scalar values and nested
 * nodes. It selects the appropriate typed {@code visit*} method based on the runtime type of the
 * value. Default implementations of the typed methods delegate to {@link #visitString(String)} via
 * {@code toString()} so that subclasses can override only the methods relevant to their format.
 *
 * <p>Values that are not natively recognized are dispatched to {@link #visitDomain(Object)}, which
 * consults the configured {@link DomainObjectMapper}: if the mapper can unwrap the value into a
 * property map, the map is visited through {@link #visitMap(Map)} — so embedded value-class
 * instances (like {@code TranslatableText}) are dispatched correctly. Otherwise the value falls
 * through to {@link #visitUnknown(Object)}.
 *
 * <p>Flat inlining of nested properties ("unwrapping", see {@link OutputUnwrapped}) is not handled
 * here: it is the responsibility of the {@link DomainObjectMapper},
 * which inlines an annotated property's sub-properties as siblings when building the property map.
 * The visitor therefore only ever sees an already-flat map.
 *
 * <p><b>Empty values are dropped.</b> While iterating a container (object fields, list items, map
 * entries, collection/array elements, and unwrapped domain-object properties) the visitor omits
 * every value that {@link #rendersEmpty renders empty} — recursively, so a nested node that becomes
 * empty after its own empties are removed is dropped as well. A value whose type carries {@link
 * OutputKeepIfEmpty} is always kept. This is the single place where output emptiness is decided;
 * {@link DomainObjectMapper} implementations only map structure (renaming/unwrapping), not
 * emptiness.
 */
public abstract class OutputVisitor {

  private final DomainObjectMapper domainObjectMapper;
  private final Translations translations;

  /** Creates a visitor without a domain object mapper, rendering the source language. */
  protected OutputVisitor() {
    this(DomainObjectMapper.NONE, Translations.SOURCE);
  }

  /**
   * Creates a visitor with the given domain object mapper, rendering the source language.
   *
   * @param domainObjectMapper the mapper for unwrapping domain objects; must not be {@code null}
   */
  protected OutputVisitor(DomainObjectMapper domainObjectMapper) {
    this(domainObjectMapper, Translations.SOURCE);
  }

  /**
   * Creates a visitor with the given domain object mapper and translation table.
   *
   * @param domainObjectMapper the mapper for unwrapping domain objects; must not be {@code null}
   * @param translations the translation table applied while rendering; must not be {@code null}
   *     (use {@link Translations#SOURCE} for the source language)
   */
  protected OutputVisitor(DomainObjectMapper domainObjectMapper, Translations translations) {
    this.domainObjectMapper = Objects.requireNonNull(domainObjectMapper);
    this.translations = Objects.requireNonNull(translations);
  }

  /** Returns the configured domain object mapper. */
  protected final DomainObjectMapper domainObjectMapper() {
    return this.domainObjectMapper;
  }

  /** Returns the translation table applied while rendering. */
  protected final Translations translations() {
    return this.translations;
  }

  /**
   * Returns {@code true} if {@code value} should be omitted from the output because it is empty.
   *
   * <p>Emptiness is evaluated <b>recursively</b>: a container (nested {@link OutputObject}/{@link
   * OutputList}, {@link Map}, {@link Collection}, array or unwrapped domain object) is empty when
   * <b>all</b> of its (recursively non-empty-filtered) children are empty. A leaf value is empty
   * when it is {@code null}, an empty {@link CharSequence}, an empty {@link Collection}/{@link
   * Map}/array, or an {@link Emptiable} reporting {@link Emptiable#isEmpty() empty} (e.g. an empty
   * {@code Text} or {@code Uri}). Numbers and booleans — including {@code 0}/{@code false} — and
   * {@link RawPhpCode} are never empty.
   *
   * <p>A value whose runtime class carries {@link OutputKeepIfEmpty} is never treated as empty, so
   * instances of that type are always rendered.
   *
   * @param value the value to inspect; may be {@code null}
   * @return {@code true} if the value should be dropped from the output
   */
  protected final boolean rendersEmpty(@Nullable Object value) {
    if (value == null) {
      return true;
    }
    if (value.getClass().isAnnotationPresent(OutputKeepIfEmpty.class)) {
      return false;
    }
    return switch (value) {
      case KeepEmpty _ -> false;
      case Emptiable e -> e.isEmpty();
      case CharSequence s -> s.isEmpty();
      case Number _ -> false;
      case Boolean _ -> false;
      case RawPhpCode _ -> false;
      case OutputList l -> allRenderEmpty(l.items());
      case OutputNode n -> allRenderEmpty(n.entries().values());
      case Map<?, ?> m -> allRenderEmpty(m.values());
      case Collection<?> c -> allRenderEmpty(c);
      case Object[] a -> allRenderEmpty(List.of(a));
      default -> {
        if (value.getClass().isArray()) {
          yield Array.getLength(value) == 0;
        }
        Map<String, Object> properties = this.domainObjectMapper.toProperties(value);
        yield properties != null && allRenderEmpty(properties.values());
      }
    };
  }

  private boolean allRenderEmpty(Iterable<?> values) {
    for (Object value : values) {
      if (!rendersEmpty(value)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the entries of {@code node} in document order, excluding those whose value {@link
   * #rendersEmpty renders empty}.
   *
   * @param node the node whose entries to filter
   * @return the non-empty entries
   */
  protected final Map<String, Object> nonEmptyEntries(OutputNode node) {
    Map<String, Object> result = new LinkedHashMap<>();
    node.entries()
        .forEach(
            (key, value) -> {
              if (!rendersEmpty(value)) {
                result.put(key, value);
              }
            });
    return result;
  }

  /**
   * Returns the items of {@code list} in order, excluding those that {@link #rendersEmpty render
   * empty}.
   *
   * @param list the list whose items to filter
   * @return the non-empty items
   */
  protected final List<OutputListItem> nonEmptyItems(OutputList list) {
    List<OutputListItem> result = new ArrayList<>();
    for (OutputListItem item : list.items()) {
      if (!rendersEmpty(item)) {
        result.add(item);
      }
    }
    return result;
  }

  /**
   * Returns the entries of {@code map} in order, excluding those whose value {@link #rendersEmpty
   * renders empty}.
   *
   * @param map the map whose entries to filter
   * @return the non-empty entries
   */
  protected final Map<?, ?> nonEmptyMap(Map<?, ?> map) {
    Map<Object, Object> result = new LinkedHashMap<>();
    map.forEach(
        (key, value) -> {
          if (!rendersEmpty(value)) {
            result.put(key, value);
          }
        });
    return result;
  }

  /**
   * Returns the elements of {@code items} in order, excluding those that {@link #rendersEmpty render
   * empty}.
   *
   * @param items the elements to filter
   * @return the non-empty elements
   */
  protected final List<Object> nonEmptyElements(Iterable<?> items) {
    List<Object> result = new ArrayList<>();
    for (Object item : items) {
      if (!rendersEmpty(item)) {
        result.add(item);
      }
    }
    return result;
  }

  /**
   * Called when the current value is an {@link OutputObject}. Default: iterates all fields via
   * {@link #iterateFields}.
   *
   * @param obj the object node to visit
   */
  public void visitObject(OutputObject obj) {
    iterateFields(obj);
  }

  /**
   * Called when the current value is an {@link OutputList}. Default: visits each item.
   *
   * @param list the list node to visit
   */
  public void visitList(OutputList list) {
    for (OutputListItem item : nonEmptyItems(list)) {
      visitListItem(item);
    }
  }

  /**
   * Called when the current value is an {@link OutputListItem}. Default: iterates all fields via
   * {@link #iterateFields}.
   *
   * @param item the list item to visit
   */
  public void visitListItem(OutputListItem item) {
    iterateFields(item);
  }

  /**
   * Iterates all entries of {@code node} and dispatches each via {@link #visitField}.
   *
   * @param node the node whose entries to iterate
   */
  protected final void iterateFields(OutputNode node) {
    nonEmptyEntries(node).forEach(this::visitField);
  }

  /**
   * Central dispatcher — resolves the runtime type of {@code value} and calls the matching {@code
   * visit*} method.
   *
   * @param key the field name, or {@code null} inside a collection or array
   * @param value the field value
   */
  public void visitField(@Nullable String key, @Nullable Object value) {
    switch (value) {
      case null -> visitNull();
      case KeepEmpty(var wrapped) -> visitField(key, wrapped);
      case OutputObject o -> visitObject(o);
      case OutputList l -> visitList(l);
      case OutputListItem i -> visitListItem(i);
      case RawPhpCode r -> visitRawPhpCode(r);
      case TranslatableText t -> visitTranslatableText(t);
      case PlainText t -> visitPlainText(t);
      case TranslatableUri u -> visitTranslatableUri(u);
      case PlainUri u -> visitPlainUri(u);
      case TranslatableSplitText s -> visitTranslatableSplitText(s);
      case ResolvedValue r -> visitResolvedValue(r);
      case String s -> visitString(s);
      case Boolean b -> visitBoolean(b);
      case Number n -> visitNumber(n);
      case Map<?, ?> m -> visitMap(m);
      case Collection<?> c -> visitCollection(c);
      case Object[] a -> visitArray(a);
      default -> visitDomain(value);
    }
  }

  /**
   * Called when the current value is a {@link java.util.Map}. Default: dispatches each entry via
   * {@link #visitField}, converting keys to strings.
   *
   * @param map the map to visit
   */
  public void visitMap(Map<?, ?> map) {
    nonEmptyMap(map).forEach((k, v) -> visitField(k == null ? null : k.toString(), v));
  }

  /**
   * Called when the current value is a {@link Collection}. Default: dispatches each element via
   * {@link #visitField} with a {@code null} key.
   *
   * @param collection the collection to visit
   */
  public void visitCollection(Collection<?> collection) {
    for (Object item : nonEmptyElements(collection)) {
      visitField(null, item);
    }
  }

  /**
   * Called when the current value is an {@code Object[]}. Default: dispatches each element via
   * {@link #visitField} with a {@code null} key.
   *
   * @param array the array to visit
   */
  public void visitArray(Object[] array) {
    for (Object item : nonEmptyElements(List.of(array))) {
      visitField(null, item);
    }
  }

  /**
   * Called for values not recognized by the built-in type switch. Consults the {@link
   * DomainObjectMapper}: if recognized, delegates to {@link #visitMap}; otherwise calls {@link
   * #visitUnknown}.
   *
   * @param value the unrecognized value
   */
  public void visitDomain(Object value) {
    @Nullable Map<String, Object> properties = this.domainObjectMapper.toProperties(value);
    if (properties == null) {
      visitUnknown(value);
    } else {
      visitMap(properties);
    }
  }

  /**
   * Called when the current value is a {@link TranslatableText}. Default: looks the text up in the
   * {@link #translations() translation table} and delegates to {@link #visitString}.
   */
  public void visitTranslatableText(TranslatableText value) {
    visitString(this.translations.translationFor(value));
  }

  /**
   * Called when the current value is a {@link PlainText}. Default: delegates to {@link #visitString}
   * via {@code toString()}.
   */
  public void visitPlainText(PlainText value) {
    visitString(value.toString());
  }

  /**
   * Called when the current value is a {@link TranslatableUri}. Default: renders the URI with the
   * {@link #translations() translation table} and delegates to {@link #visitString}.
   */
  public void visitTranslatableUri(TranslatableUri value) {
    visitString(value.render(this.translations));
  }

  /**
   * Called when the current value is a {@link PlainUri}. Default: delegates to {@link #visitString}
   * via {@code toString()}.
   */
  public void visitPlainUri(PlainUri value) {
    visitString(value.toString());
  }

  /**
   * Called when the current value is a {@link TranslatableSplitText}. Default: renders it with the
   * {@link #translations() translation table} and delegates to {@link #visitString}.
   */
  public void visitTranslatableSplitText(TranslatableSplitText value) {
    visitString(value.render(this.translations));
  }

  /**
   * Called when the current value is a {@link ResolvedValue}. Default: visits the inner value, or
   * calls {@link #visitNull()} if the resolved value is empty.
   */
  public void visitResolvedValue(ResolvedValue value) {
    if (value.isEmpty()) {
      visitNull();
    } else {
      visitField(null, value.value());
    }
  }

  /**
   * Called when the current value is a {@link RawPhpCode}. Default: delegates to {@link
   * #visitUnknown}.
   */
  public void visitRawPhpCode(RawPhpCode value) {
    visitUnknown(value);
  }

  /**
   * Called for plain string values. Default implementation is a no-op.
   *
   * @param value the string value
   */
  public void visitString(String value) {}

  /**
   * Called for numeric values. Default implementation is a no-op.
   *
   * @param value the numeric value
   */
  public void visitNumber(Number value) {}

  /**
   * Called for boolean values. Default implementation is a no-op.
   *
   * @param value the boolean value
   */
  public void visitBoolean(Boolean value) {}

  /** Called for {@code null} values. Default implementation is a no-op. */
  public void visitNull() {}

  /**
   * Called for values not recognized by any other dispatch path. Default implementation is a no-op.
   *
   * @param value the unrecognized value
   */
  public void visitUnknown(Object value) {}
}
