# Extending Assemblers

> **Type:** How-To

This document describes three patterns that allow assemblers to be extended or overridden per
project without changes to the core code. Patterns 1 and 2 are based on `@Assembler` and
`AssemblerFactory`; Pattern 3 adds project-specific fields to the produced model.

| Pattern                                    | When to use                                                                                               |
|--------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| **Pattern 1: Type-driven dispatching**     | Multiple implementations for different types; which one applies is only determined at runtime in the Resolver |
| **Pattern 2: Direct override**             | Exactly one implementation; customer projects should be able to replace or extend it                      |
| **Pattern 3: Additional model fields**     | A value type must carry extra, project-specific fields without changing the core record                   |

---

## Core principle: delegation, not inheritance

All built-in assembler implementations are `final`. Projects **never extend an assembler to
customize it** — they always **delegate**. An override implements the same *interface* as the
built-in, registers it with a higher `@Assembler` priority, and calls the built-in implementation
through an injected delegate.

This is not just a style rule; it is enforced by how the factory resolves implementations:

- **The dispatch type must be an interface.** `AssemblerFactory.create(key, type)` selects
  candidates by *assignability* to `type` (`type.isAssignableFrom(candidate)`) and picks the highest
  `priority` for that `key`. If the dispatch type were a `final` concrete class, no override could
  ever be assignable to it — the factory would never select it. Therefore every overridable assembler
  is dispatched via an interface (e.g. `LinkAssembler`, `ImageAssembler`, `LinkListAssembler`), and
  the built-in class (e.g. `DefaultLinkListAssembler`) is a `final` implementation of it.
- **Never re-enter the factory for the same key.** An override must obtain the built-in instance by
  **injecting the concrete `Default…` class directly** — not via `factory.create(sameKey, …)`, which
  would resolve back to the override itself and recurse infinitely. Injecting the concrete class lets
  the DI container (Guice) construct the built-in directly, bypassing key/priority resolution.

---

## Pattern 1: Type-driven dispatching

### Overview

An aggregation run must build objects of different types from the same source data. A type field in
the Resolver determines at runtime which assembler is chosen. At the same time, customer projects
should be able to add new types or replace existing implementations.

The solution consists of three building blocks:

| Building block            | Responsibility                                                            |
|---------------------------|----------------------------------------------------------------------------|
| **Type field in Resolver**| Determines at runtime which assembler is chosen                           |
| **`@Assembler`**          | Registers a class under a unique key                                       |
| **`AssemblerFactory`**    | Resolves the key, returns the implementation with the highest priority     |

### Architecture

Using the Link assembler family as an example:

```mermaid
sequenceDiagram
    participant LL as DefaultLinkListAssembler
    participant D as LinkAssemblerDispatcher
    participant R as Resolver
    participant F as AssemblerFactory
    participant A as concrete LinkAssembler
    LL ->> D: resolve(itemSource, options)
    D ->> R: value("sp_linkType").asString("internal")
    R -->> D: e.g. "internal" or "external"
    D ->> F: create("link.internal", LinkAssembler.class)
    F ->> F: find all @Assembler("link.internal"),<br/>choose highest priority
    F -->> D: matching Assembler instance
    D ->> A: resolve(source, options)
    A -->> D: Optional[Link]
    D -->> LL: Optional[Link]
```

### Classes involved

**`LinkAssembler<T extends Link>`** — the common interface of all Link assemblers:

```java
public interface LinkAssembler<T extends Link> {
    Optional<T> assemble(Resolver source, LinkOptions options);
}
```

**`LinkAssemblerDispatcher`** — the central dispatcher. It reads the type from the Resolver,
builds the key, and delegates to the factory:

```java
public class LinkAssemblerDispatcher {

    private final AssemblerFactory assemblerFactory;

    @Inject
    LinkAssemblerDispatcher(AssemblerFactory assemblerFactory) {
        this.assemblerFactory = assemblerFactory;
    }

    public Optional<? extends Link> assemble(Resolver source, LinkOptions options) {
        String linkType = source.value("sp_linkType").asString("internal");
        LinkAssembler<?> assembler =
                this.assemblerFactory.create("link." + linkType, LinkAssembler.class);
        return assembler.assemble(source, options);
    }
}
```

**`InternalLinkAssembler`** — the built-in assembler for internal links, registered under
`"link.internal"` with the default priority 0:

```java

@Assembler("link.internal")
public final class InternalLinkAssembler implements LinkAssembler<Link.InternalLink> {

    @Inject
    InternalLinkAssembler(ChannelUriProvider uriProvider, ChannelProvider channelProvider,
                          HeadlineAssembler headlineAssembler) { ...}

    @Override
    public Optional<Link.InternalLink> assemble(Resolver source, LinkOptions options) {
        // reads sp_link.link, sp_linkText, sp_linkNewWindow, ...
    }
}
```

**`Link`** — the interface that all link types must implement in common. Concrete types
are Java records:

```java
public interface Link {
    String modelType();

    Uri url();

    Text label();

    boolean newWindow();

    record InternalLink(
            @JsonProperty("modelType") String modelType,
            @JsonProperty("iesId") int iesId,
            @JsonProperty("objectType") String objectType,
            @JsonProperty("resourceUrl") String resourceUrl,
            @JsonProperty("url") Uri url,
            @JsonProperty("label") Text label,
            @JsonProperty("newWindow") boolean newWindow)
            implements Link {
    }
}
```

### Use case 1: Adding a new link type

A new link type (e.g. `"external"` for external URLs) requires three steps.

**Step 1:** Add a new record in `Link.java`:

```java
public interface Link {
    // ... existing methods and records ...

    record ExternalLink(
            @JsonProperty("modelType") String modelType,
            @JsonProperty("url") Uri url,
            @JsonProperty("label") Text label,
            @JsonProperty("newWindow") boolean newWindow)
            implements Link {
    }
}
```

**Step 2:** Implement the assembler and register it with the matching key. The key
must match the pattern `"link." + sp_linkType`:

```java

@Assembler("link.external")
public final class ExternalLinkAssembler implements LinkAssembler<Link.ExternalLink> {

    @Inject
    public ExternalLinkAssembler() {
    }

    @Override
    public Optional<Link.ExternalLink> assemble(Resolver source, LinkOptions options) {
        String rawUrl = source.value("sp_externalUrl").asString("");
        if (rawUrl.isBlank()) {
            return Optional.empty();
        }

        Uri url = Uri.of(rawUrl);
        Text label = source.value("sp_linkText").asText(PlainText.EMPTY).translatable();
        boolean newWindow = source.value("sp_linkNewWindow").asBoolean(true);

        return Optional.of(new Link.ExternalLink("content.link.external", url, label, newWindow));
    }
}
```

**Step 3:** In the CMS object, set the `sp_linkType` field to `"external"`. The
`LinkAssemblerDispatcher` then automatically routes to the `ExternalLinkAssembler` — no
further code change needed.

### Use case 2: Overriding an existing link type

To replace a built-in implementation per project, the same key is used with a
**higher priority**. The built-in `InternalLinkAssembler` has `priority = 0`
(default). An override implementation uses `priority = 100`:

```java

@Assembler(value = "link.internal", priority = 100)
public final class ProjectInternalLinkAssembler implements LinkAssembler<Link.InternalLink> {

    private final ChannelUriProvider uriProvider;
    private final ChannelProvider channelProvider;
    private final HeadlineAssembler headlineAssembler;
    private final ProjectLinkEnricher enricher;    // project-specific extension

    @Inject
    public ProjectInternalLinkAssembler(
            ChannelUriProvider uriProvider,
            ChannelProvider channelProvider,
            HeadlineAssembler headlineAssembler,
            ProjectLinkEnricher enricher) {
        this.uriProvider = uriProvider;
        this.channelProvider = channelProvider;
        this.headlineAssembler = headlineAssembler;
        this.enricher = enricher;
    }

    @Override
    public Optional<Link.InternalLink> assemble(Resolver source, LinkOptions options) {
        // custom logic, e.g. read additional fields or resolve the channel differently
        ...
    }
}
```

The `AssemblerFactory` automatically selects this class because its `priority` value is higher. The
built-in implementation remains unchanged on the classpath but is no longer used.

Both the built-in and the override are `final` and implement the shared `LinkAssembler` interface —
there is no `extends`. The example above *replaces* the built-in entirely. If you only want to
*extend* it (reuse the built-in result and enrich it), inject the built-in `InternalLinkAssembler`
as a delegate and call it, exactly as shown for `DefaultLinkListAssembler` in Pattern 2.

---

## Pattern 2: Direct override

### Overview

There is exactly one assembler for a task — no type field in the Resolver, no dispatch. Nonetheless,
customer projects can replace or extend the assembler without touching the Aggregator.

The crucial difference from direct DI: the Aggregator does not inject the assembler itself,
but the `AssemblerFactory`. For each aggregation run, it obtains a fresh instance by key. Which
concrete class is returned is decided by the factory based on the registered
`@Assembler` annotations and their priority.

### Setup

Using `LinkListAssembler` and `LinkListAggregator` as an example.

The extension point is the **`LinkListAssembler` interface**; the built-in **implementation** is
`final` and registered with `@Assembler`:

```java
public interface LinkListAssembler {
    Optional<LinkList> assemble(Resolver source, LinkListOptions options);
}

@Assembler("linkList")
public final class DefaultLinkListAssembler implements LinkListAssembler {

    private final LinkAssemblerDispatcher linkAssembler;
    private final AggregatorErrorHandler errorHandler;

    @Inject
    public DefaultLinkListAssembler(LinkAssemblerDispatcher linkAssembler,
                                    AggregatorErrorHandler errorHandler) {
        this.linkAssembler = linkAssembler;
        this.errorHandler = errorHandler;
    }

    @Override
    public Optional<LinkList> assemble(Resolver source, LinkListOptions options) {
        Text headline = source.value("sp_headline").asText(PlainText.EMPTY).translatable();
        String boxType = source.value("sp_linkBoxType").asString(options.box().defaultType());
        List<Link> items = resolveItemList(source, options.linkOptions());
        if (items.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new LinkList("content.linkList", headline, boxType, items));
    }
}
```

The **aggregator** obtains the assembler per run via the factory:

```java
public class LinkListAggregator implements Aggregator, OptionsAware<LinkListOptions> {

    private final AssemblerFactory assemblerFactory;
    private LinkListOptions options;

    @Inject
    public LinkListAggregator(AssemblerFactory assemblerFactory) {
        this.assemblerFactory = assemblerFactory;
    }

    @Override
    public void aggregate(Resolver source, OutputNode output) throws AggregatorException {
        LinkListAssembler linkListAssembler =
                this.assemblerFactory.create("linkList", LinkListAssembler.class);
        linkListAssembler.assemble(source, this.options)
                .ifPresent(linkList -> output.put("model", linkList));
    }
}
```

### Override in a customer project

An implementation of `LinkListAssembler` with a higher priority value automatically takes control —
without any change to `LinkListAggregator`. Instead of inheriting from the built-in, it **injects
`DefaultLinkListAssembler` as a delegate** and enriches its result:

```java

@Assembler(value = "linkList", priority = 100)
public final class ProjectLinkListAssembler implements LinkListAssembler {

    private final DefaultLinkListAssembler delegate;   // concrete built-in, injected directly
    private final ProjectTeaser teaser;

    @Inject
    public ProjectLinkListAssembler(DefaultLinkListAssembler delegate, ProjectTeaser teaser) {
        this.delegate = delegate;
        this.teaser = teaser;
    }

    @Override
    public Optional<LinkList> assemble(Resolver source, LinkListOptions options) {
        // call the built-in implementation and enrich the result, or replace it entirely
        return this.delegate.assemble(source, options).map(list -> enrich(list, source));
    }

    private LinkList enrich(LinkList list, Resolver source) {
        // project-specific enrichment
        ...
    }
}
```

> **Do not** call `assemblerFactory.create("linkList", LinkListAssembler.class)` from inside the
> override — it resolves back to this very class (highest priority) and recurses infinitely. Inject
> the concrete `DefaultLinkListAssembler` instead; the DI container constructs it directly, bypassing
> key/priority resolution.

---

## Pattern 3: Additional model fields

### Overview

Patterns 1 and 2 let a project decide *which* assembler runs. But the value types an assembler
produces are **final Java records** — a customer override can return a record, yet it cannot add a
field to an existing record type (records cannot be subclassed).

To add project-specific fields **without changing the core record**, a value type exposes an
extension slot annotated with Jackson's `@JsonUnwrapped`. The `DomainObjectMapper` inlines its
sub-properties **flat, at the top level** of the model (see
[Visitor → Flattening properties](../reference/visitor.md)). The slot carries a typed object, so no
`Map` is needed at the call site. Because the marker sits on the declaration, a `null` slot simply
contributes nothing — no dangling `extension` key is rendered.

### Convention: an `Extensible` base interface

Consumer projects typically introduce a small base interface so every model carries the slot
uniformly:

```java
public interface Extensible {
    Object extension();
}
```

A value record adds the slot as a component annotated `@JsonUnwrapped` — **no `@JsonProperty`**,
because the slot's own key is dropped during inlining and the component name is never rendered:

```java
public record LinkList(
        @JsonProperty("modelType") String modelType,
        @JsonProperty("headline") Text headline,
        @JsonProperty("linkBoxType") String linkBoxType,
        @JsonProperty("items") List<Link> items,
        @JsonUnwrapped Object extension)
        implements Extensible {
}
```

The built-in assembler passes `null` (contributes nothing — no dangling key):

```java
return Optional.of(new LinkList("content.linkList", headline, boxType, items, null));
```

### Adding fields in a customer project

Combine with Pattern 2: register an override under the same key with a higher priority, reuse the
built-in implementation via delegation, and place a **typed** extension object into the slot:

```java
public record LinkListBadge(String label, int count) {
}

@Assembler(value = "linkList", priority = 100)
public final class ProjectLinkListAssembler implements LinkListAssembler {

    private final DefaultLinkListAssembler delegate;

    @Inject
    public ProjectLinkListAssembler(DefaultLinkListAssembler delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<LinkList> assemble(Resolver source, LinkListOptions options) {
        return this.delegate.assemble(source, options)
                .map(list -> new LinkList(
                        list.modelType(), list.headline(), list.linkBoxType(), list.items(),
                        new LinkListBadge("new", list.items().size())));
    }
}
```

The model then renders with `label` and `count` as **top-level** fields next to `modelType`,
`headline`, … instead of nested under an `extension` key.

> **Note:** the slot is typed `Object` here to accept any extension payload (as the removed
> `Unwrapped` carrier did). `@JsonUnwrapped` on an `Object`/`Map` field is inlined by the
> introspecting `JacksonDomainObjectMapper`, even though plain Jackson *serialization* only supports
> it on concrete bean types. Projects that always use one payload type can declare that type
> directly instead of `Object`.

---

## Important notes

### `AssemblerFactory` creates a fresh instance per call

Every call to `assemblerFactory.create(key, clazz)` returns a **fresh instance**. Assemblers
may therefore hold state in fields without having to worry about thread safety or run
isolation.

### Dependency Injection via `@Inject`

Assemblers are instantiated by the DI container. Dependencies are declared in the constructor
and wired in via `@Inject`. No manual wiring is needed, as long as the dependencies are registered
in the DI container.

### Fault tolerance in list processing

The `DefaultLinkListAssembler` catches `AggregatorException` errors individually per list item and forwards
them to the `AggregatorErrorHandler`. A single failed link does not abort the
entire list:

```java
void example() {
    for (Resolver itemSource : source.resolveList("sp_link_iterate")) {

        try {
            this.linkAssembler.assemble(itemSource, options).ifPresent(items::add);

        } catch (AggregatorException e) {
            this.errorHandler.handle(e); // logs, skips this item
        }
    }
}
```

Custom assemblers can throw `AggregatorException` when a required field is missing or a value
is invalid — the caller decides how to handle it.

### Both patterns can be combined

Pattern 1 and Pattern 2 are not mutually exclusive. In the Link assembler family, Pattern 1 is
already active (`LinkAssemblerDispatcher`). If Pattern 2 is additionally applied to `LinkListAssembler`,
a customer project can override both individual link types and the entire list logic independently
of one another.
