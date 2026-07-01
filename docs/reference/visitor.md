# Visitor

> **Type:** Reference
> **Role:** Processes a finished OutputNode tree without modifying it.
> **Behavior:** Traverses the target structure and calls a specialized
> `visit*` method per node type.

## What is a Visitor?

An `OutputVisitor` is the **read counterpart to the Aggregator**: whereas an Aggregator *writes* the
`OutputNode`, a Visitor *reads* it back out. Because the OutputNode is deliberately
**rendering-neutral** (it describes structure, not format), the Visitor is the place where
different **concrete representations** are produced from the same structure – PHP array syntax,
JSON, a simple `Map<String, Object>`, or a list of all translatable texts for a
translation pipeline.

The Visitor follows the classic **Visitor pattern (Double Dispatch)**: `OutputObject`,
`OutputList` and `OutputListItem` each implement an `accept(visitor)` method that calls the
matching `visit*` method on the Visitor. Within the Visitor, a central
dispatcher (`visitField`) routes to typed methods depending on the value class (`visitTranslatableText`,
`visitUri`, `visitNumber`, …) – format visitors override only the methods that are relevant to their
representation.

## Characteristics

- **Read view:** The Visitor does not modify the OutputNode. It produces a result (String,
  Map, List, …) or writes into an external `Writer`.
- **Format-specific:** One concrete subclass per output format (`PhpArrayWriter`, `JsonWriter`,
  `MapConverter`, `TranslatableTextCollector`).
- **Type-safe per value class:** Dedicated methods for `PlainText`, `TranslatableText`,
  `PlainUri`, `TranslatableUri`, `TranslatableSplitText`, `ResolvedValue`, `RawPhpCode` enable
  format-specific handling – e.g. the `JsonWriter` renders a `TranslatableText`
  differently by default than the `PhpArrayWriter`.
- **Sensible defaults:** Anyone who does not handle a value class explicitly gets a
  `toString()`-based fallback representation – subclasses override only what they really
  want to output differently.
- **Raw PHP via marker:** The type-safe `RawPhpCode` marker replaces the former
  `:php` key suffix. Values of type `RawPhpCode` are emitted unchanged (without
  quoting) by the `PhpArrayWriter`; other visitors treat them as an opaque value.
- **Extensible:** Custom output formats or analyses are created by subclassing
  `OutputVisitor`.

## Bundled visitors

| Visitor                     | Package          | Result                                              | Use                                      |
|-----------------------------|------------------|-----------------------------------------------------|------------------------------------------|
| `PhpArrayWriter`            | `output.format`  | PHP array syntax into a `Writer`                    | File serialization for PHP consumers     |
| `JsonWriter`                | `output.format`  | Compact JSON into a `Writer`                        | REST/frontends/logging                   |
| `MapConverter`              | `output.convert` | `Map<String, Object>` / `List<Map<String, Object>>` | Interop with map-based APIs              |
| `TranslatableTextCollector` | `output.collect` | `List<TranslatableText>`                            | Translation pipeline                     |

## Invocation: one core, two entry points

All visitors run internally through **the same mechanism**: `output.accept(visitor)`. This method
returns `void` everywhere — the Visitor collects its result in its own state. The only
difference at the API surface is *where the result flows to* and thus *how you retrieve it*:

- **Writer (`PhpArrayWriter`, `JsonWriter`):** The target is the `Writer` that the caller already
  holds. `output.accept(writer)` is enough — the result is then in the `Writer`. There is
  deliberately no getter, because the `Writer` *is* the result.
- **Collectors/converters (`MapConverter`, `TranslatableTextCollector`):** The result ends up in the
  internal state and needs a return path. `toMap()`/`toList()` resp. `collect()` are
  **convenience wrappers** around exactly the same `accept` core — they encapsulate the trio
  `reset state → accept → return typed result`:

  ```java
  public Map<String, Object> toMap(OutputObject root) {
    reset();
    root.accept(this);   // ← the same accept path as with the Writers
    return (Map<String, Object>) this.result;
  }
  ```

  This is more discoverable and safer than a raw `accept` call followed by a getter (no
  forgotten `reset`, no unchecked cast on the caller's side).

## Example

```java
// 1. Write a PHP array into a file
try (Writer w = Files.newBufferedWriter(path)) {
  output.accept(new PhpArrayWriter(w));
}

// 2. Render JSON into a String
StringWriter sw = new StringWriter();
output.accept(new JsonWriter(sw));
String json = sw.toString();

// 3. Convert into a simple Map (toMap encapsulates accept)
Map<String, Object> map = new MapConverter().toMap(output);

// 4. Collect all translatable texts (language-agnostic, collect encapsulates accept)
List<TranslatableText> texts = new TranslatableTextCollector().collect(output);
// → the target language is only added during rendering via the translations table
//   (see ../how-to/translations-pipeline.md)
```

## Distinctions

- A Visitor is **not an Aggregator**: it does not produce an OutputNode, it *consumes* it.
- A Visitor is **not a Resolver**: it does not read from a source, but from the already
  finished target tree.
- A Visitor is **not an OutputNode renderer with a fixed format**: the format decision lies in
  the concrete Visitor subclass, not in the OutputNode itself.

## Domain objects: the `DomainObjectMapper` extension point

Aggregator pipelines often write finished domain objects (e.g. a `Link` with `getLabel():
TranslatableText`) directly into the OutputNode. The Visitor does not know these project-specific classes
— without further configuration they end up in the output via `visitUnknown` as a `toString()` fallback,
and embedded `TranslatableText` instances would be invisible to the `TranslatableTextCollector`.

For this there is the `DomainObjectMapper` as a **Strategy-Hook**:

```java
public interface DomainObjectMapper {
  Map<String, Object> toProperties(Object value);   // null = "not a domain object"
  DomainObjectMapper NONE = v -> null;
}
```

The mapper provides the properties of a domain object as a `Map<String, Object>` — importantly: with the
**original values** (e.g. the `TranslatableText` object itself, not a serialized copy).
The Visitor then passes the map through `visitMap`, so that all inner values run through the normal
dispatcher and typed `visit*` methods for `TranslatableText`, `Uri` etc. take effect.

A custom `DomainObjectMapper` implementation is passed to the Visitor when it is created:

```java
DomainObjectMapper mapper = value -> {
  if (value instanceof Link link) {
    return Map.of("label", link.getLabel(), "url", link.getUrl());
  }
  return null;
};

output.accept(new JsonWriter(writer, mapper));
output.accept(new PhpArrayWriter(writer, mapper));
new MapConverter(mapper).toMap(output);
new TranslatableTextCollector(mapper).collect(output);
```

This way:

- `Link` objects are serialized by the JSON/PHP writer as structured objects/associative arrays
- `TranslatableText` properties inside domain objects show up in the translation list
- the OutputNode stays rendering-neutral — the mapper strategy decides *how* domain objects
  are presented

Anyone who does not need a mapper uses the null default `DomainObjectMapper.NONE` (automatically
set when no mapper is passed).

## Flattening properties: `@JsonUnwrapped`

Normally every property is rendered under its own key — a `Map` or a domain object as a property
value becomes a **nested** object. Sometimes a property's *own* properties should instead appear
**inline, at the level of the surrounding object**. This is expressed with Jackson's
[`@JsonUnwrapped`](https://fasterxml.github.io/jackson-annotations/javadoc/2.14/com/fasterxml/jackson/annotation/JsonUnwrapped.html)
on the property:

```java
public record ContentImage(
    ImageDescription description,
    @JsonUnwrapped Extension extension) {}   // inlined flat, no "extension" key
```

Flattening is **not** the visitor's job — the visitor only ever sees an already-flat property map.
It is the `DomainObjectMapper` that inlines: the Jackson-backed `JacksonDomainObjectMapper` detects
`@JsonUnwrapped` during bean introspection and, instead of adding the property under its own key,
merges the nested object's properties (value-preserving, so typed values like `TranslatableText`
survive) as **sibling entries at the current level**.

The key advantage over a value-level carrier: because the marker sits on the **declaration**, it is
honored even when the property value is `null` — a `null` `@JsonUnwrapped` property simply
contributes nothing and leaves **no dangling key**.

Notes:

- **No visitor changes needed.** The flat map flows through the normal `visitField` dispatcher, so
  every format visitor (`PhpArrayWriter`, `JsonWriter`, `MapConverter`, …) renders it automatically.
- **Typed content, flat output.** The unwrapped property may be any domain object (resolved
  recursively) or a plain `Map`; its fields are rendered flat while keeping their typed values.
- **Prefix/suffix.** A configured `@JsonUnwrapped(prefix = …, suffix = …)` is applied to the
  inlined keys.
- **Sibling keys.** Because the property's key is dropped, the inlined keys become siblings of the
  surrounding object's fields — the author is responsible for avoiding name collisions.
- **Primary use:** extensible output models — a value type exposes an `@JsonUnwrapped` slot so
  projects can add top-level fields without subclassing final records. See
  [Extending Assemblers](../how-to/assembler-customization.md).
