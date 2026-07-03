package com.sitepark.ies.aggregator.output;

import com.sitepark.ies.aggregator.output.format.RawPhpCode;
import com.sitepark.ies.aggregator.value.ResolvedValue;
import com.sitepark.ies.aggregator.value.text.PlainText;
import com.sitepark.ies.aggregator.value.text.TranslatableSplitText;
import com.sitepark.ies.aggregator.value.text.TranslatableText;
import com.sitepark.ies.aggregator.value.text.Translations;
import com.sitepark.ies.aggregator.value.uri.PlainUri;
import com.sitepark.ies.aggregator.value.uri.TranslatableUri;
import java.util.Collection;
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
    for (OutputListItem item : list.items()) {
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
    node.entries().forEach(this::visitField);
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
    map.forEach((k, v) -> visitField(k == null ? null : k.toString(), v));
  }

  /**
   * Called when the current value is a {@link Collection}. Default: dispatches each element via
   * {@link #visitField} with a {@code null} key.
   *
   * @param collection the collection to visit
   */
  public void visitCollection(Collection<?> collection) {
    for (Object item : collection) {
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
    for (Object item : array) {
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
