package com.sitepark.ies.aggregator.output;

import com.sitepark.ies.aggregator.output.format.Code;
import com.sitepark.ies.aggregator.output.format.RawPhpCode;
import com.sitepark.ies.aggregator.value.Emptiable;
import com.sitepark.ies.aggregator.value.ResolvedValue;
import com.sitepark.ies.aggregator.value.text.PlainText;
import com.sitepark.ies.aggregator.value.text.TranslatableContainer;
import com.sitepark.ies.aggregator.value.text.TranslatableSplitText;
import com.sitepark.ies.aggregator.value.text.TranslatableText;
import com.sitepark.ies.aggregator.value.text.Translations;
import com.sitepark.ies.aggregator.value.uri.PlainUri;
import com.sitepark.ies.aggregator.value.uri.TranslatableUri;
import java.lang.reflect.Array;
import java.time.Instant;
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
 * empty after its own empties are removed is dropped as well. Which empty values survive anyway is
 * the one configurable part of that rule: the {@link EmptyValuePolicy} decides it by the value's
 * runtime class, defaulting to {@link EmptyValuePolicy#ANNOTATED} (the type-level {@link
 * OutputKeepIfEmpty}). This is the single place where output emptiness is decided; {@link
 * DomainObjectMapper} implementations only map structure (renaming/unwrapping), not emptiness.
 */
public abstract class OutputVisitor {

  private final DomainObjectMapper domainObjectMapper;
  private final Translations translations;
  private final EmptyValuePolicy emptyValuePolicy;

  /**
   * The policy in force right now. Equal to {@link #emptyValuePolicy} while the visitor walks the
   * values it was handed, and {@link EmptyValuePolicy#insideDomainObject()} of the enclosing one
   * while it walks the properties of a domain object — see {@link #insideDomainObject}.
   */
  private EmptyValuePolicy activePolicy;

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
   * Creates a visitor with the given domain object mapper and empty-value policy, rendering the
   * source language.
   *
   * @param domainObjectMapper the mapper for unwrapping domain objects; must not be {@code null}
   * @param emptyValuePolicy the policy deciding which empty values are rendered anyway; must not be
   *     {@code null}
   */
  protected OutputVisitor(
      DomainObjectMapper domainObjectMapper, EmptyValuePolicy emptyValuePolicy) {
    this(domainObjectMapper, Translations.SOURCE, emptyValuePolicy);
  }

  /**
   * Creates a visitor with the given domain object mapper and translation table.
   *
   * @param domainObjectMapper the mapper for unwrapping domain objects; must not be {@code null}
   * @param translations the translation table applied while rendering; must not be {@code null}
   *     (use {@link Translations#SOURCE} for the source language)
   */
  protected OutputVisitor(DomainObjectMapper domainObjectMapper, Translations translations) {
    this(domainObjectMapper, translations, EmptyValuePolicy.ANNOTATED);
  }

  /**
   * Creates a visitor with the given domain object mapper, translation table and empty-value policy.
   *
   * @param domainObjectMapper the mapper for unwrapping domain objects; must not be {@code null}
   * @param translations the translation table applied while rendering; must not be {@code null}
   *     (use {@link Translations#SOURCE} for the source language)
   * @param emptyValuePolicy the policy deciding which empty values are rendered anyway; must not be
   *     {@code null} (use {@link EmptyValuePolicy#ANNOTATED} for the default behavior)
   */
  protected OutputVisitor(
      DomainObjectMapper domainObjectMapper,
      Translations translations,
      EmptyValuePolicy emptyValuePolicy) {
    this.domainObjectMapper = Objects.requireNonNull(domainObjectMapper);
    this.translations = Objects.requireNonNull(translations);
    this.emptyValuePolicy = Objects.requireNonNull(emptyValuePolicy);
    this.activePolicy = this.emptyValuePolicy;
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
   * Returns the configured policy deciding which empty values are rendered anyway.
   *
   * <p>The <i>configured</i> one, not necessarily the one in force: below a domain object the
   * visitor applies {@link EmptyValuePolicy#insideDomainObject()} instead.
   */
  protected final EmptyValuePolicy emptyValuePolicy() {
    return this.emptyValuePolicy;
  }

  /**
   * Runs {@code body} with the policy that governs the properties of a domain object.
   *
   * <p>Nests: the switch applies to everything below, including further domain objects, and the
   * enclosing policy is restored on the way out — which is what makes a rich text holding a link
   * holding another rich text come out right.
   *
   * @param body what to render below the domain object
   */
  // The assignment is what body.run() reads, through the visit methods it calls; PMD's dataflow
  // does not follow that and sees only the restore in the finally block.
  @SuppressWarnings("PMD.UnusedAssignment")
  private void insideDomainObject(Runnable body) {
    EmptyValuePolicy enclosing = this.activePolicy;
    this.activePolicy = enclosing.insideDomainObject();
    try {
      body.run();
    } finally {
      this.activePolicy = enclosing;
    }
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
   * <p>A value the configured {@link EmptyValuePolicy} keeps is never treated as empty, so instances
   * of that class are always rendered; a {@code null} is kept when the policy {@link
   * EmptyValuePolicy#keepNull() keeps nulls}. The default policy {@link EmptyValuePolicy#ANNOTATED}
   * keeps the types carrying {@link OutputKeepIfEmpty} and drops {@code null}.
   *
   * @param value the value to inspect; may be {@code null}
   * @return {@code true} if the value should be dropped from the output
   */
  protected final boolean rendersEmpty(@Nullable Object value) {
    if (value == null) {
      return !this.activePolicy.keepNull();
    }
    if (this.activePolicy.keepIfEmpty(value.getClass())) {
      return false;
    }
    return switch (value) {
      case KeepEmpty _ -> false;
      case Emptiable e -> e.isEmpty();
      case CharSequence s -> s.isEmpty();
      case Number _ -> false;
      case Boolean _ -> false;
      case Instant _ -> false;
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
        // Judged the way it will be written: under the policy that governs the inside of a domain
        // object, so this decision and visitDomain cannot disagree.
        yield properties != null && allRenderEmptyInsideDomainObject(properties.values());
      }
    };
  }

  /** {@link #allRenderEmpty} under the policy that governs the properties of a domain object. */
  private boolean allRenderEmptyInsideDomainObject(Iterable<?> values) {
    EmptyValuePolicy enclosing = this.activePolicy;
    this.activePolicy = enclosing.insideDomainObject();
    try {
      return allRenderEmpty(values);
    } finally {
      this.activePolicy = enclosing;
    }
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
      case Code c -> visitCode(c);
      case TranslatableText t -> visitTranslatableText(t);
      case PlainText t -> visitPlainText(t);
      case TranslatableUri u -> visitTranslatableUri(u);
      case PlainUri u -> visitPlainUri(u);
      case TranslatableContainer c -> visitTranslatableContainer(c);
      case ResolvedValue r -> visitResolvedValue(r);
      case String s -> visitString(s);
      case Boolean b -> visitBoolean(b);
      case Number n -> visitNumber(n);
      case Instant i -> visitInstant(i);
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
      insideDomainObject(() -> visitMap(properties));
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
   * Called when the current value is a {@link TranslatableContainer} (e.g. {@link
   * TranslatableSplitText} or a domain-specific translatable value). Default: renders it with the
   * {@link #translations() translation table} and re-dispatches the result through {@link
   * #visitField}, so a container may render either a scalar (e.g. a {@code String}) or a structured
   * output value.
   *
   * @param value the translatable container to render
   */
  public void visitTranslatableContainer(TranslatableContainer value) {
    // The container is the aggregator's own value and so is whatever it renders to, even when that
    // is a whole model: the switch belongs here, not one level further down.
    insideDomainObject(() -> visitField(null, value.render(this.translations)));
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
   * Called when the current value is a {@link Code} — either raw code or plain content. Default:
   * delegates to {@link #visitString} with {@link Code#code()}, so it is rendered like any other
   * string. A format that emits raw code verbatim overrides this and switches on the variant.
   */
  public void visitCode(Code value) {
    visitString(value.code());
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

  /**
   * Called when the current value is an {@link Instant}. Default: delegates to {@link
   * #visitString(String)} with the ISO-8601 representation of the instant. Formats that expect a
   * different representation override this — the {@code PhpArrayWriter} writes epoch seconds as a
   * bare number.
   *
   * @param value the instant value
   */
  public void visitInstant(Instant value) {
    visitString(value.toString());
  }

  /** Called for {@code null} values. Default implementation is a no-op. */
  public void visitNull() {}

  /**
   * Called for values not recognized by any other dispatch path. Default implementation is a no-op.
   *
   * @param value the unrecognized value
   */
  public void visitUnknown(Object value) {}
}
