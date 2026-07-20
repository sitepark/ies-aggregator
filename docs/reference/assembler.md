# Assembler

> **Type:** Reference
> **Role:** Builds a typed domain object from the values of a Resolver.
> **Behavior:** Reads multiple fields, combines them and returns a finished value object (Value
> Object).

## What is an Assembler?

An `Assembler` is a reusable component that assembles a **single, typed domain object** from the
data of a Resolver. The term is derived from Martin Fowler's *[Data
Transfer Object Assembler](https://martinfowler.com/eaaCatalog/dataTransferObject.html)* pattern: a class
that **assembles** a target representation from
source data.

While a Resolver only delivers raw values, an Assembler raises them to a **business level**:
from the fields `sp_link.link`, a URI resolution and a label, a complete
`Link` object is created, for example - including all domain-specific rules (default values, validation,
channel consideration, optional fields).

Assemblers are the **business building blocks** of the aggregation. They encapsulate knowledge about *how* a
specific domain object is created from a source, and are independent of *which*
output context it is later embedded into.

## Characteristics

- **Single Responsibility:** An Assembler builds **exactly one** type of domain object
  (`LinkAssembler` → `Link`, `LinkListAssembler` → `LinkList`).
- **Returns typed result:** The result is a finished value object, typically as
  `Optional<T>` if it can legitimately be empty.
- **Uniform signature:** Every assembler implements `Assembler<REQ, V>` with a single
  `assemble(REQ request, @Nullable V previous)` method. `REQ` is a feature-specific *request* record
  that bundles the assembler's inputs (the source resolver, field names, options); bundling keeps the
  signature stable when further inputs are added later. `previous` carries the running value when
  assemblers are chained (see below).
- **Reusable:** Multiple aggregators can use the same Assembler.
- **Composable:** Assemblers may use other Assemblers (e.g. `LinkListAssembler` uses the
  `LinkAssembler` for each element).
- **Does not write to the OutputNode:** Assemblers do not know the OutputNode - they only return the
  finished object. What happens with it is decided by the caller (usually an Aggregator).
- **Fault tolerance possible:** Via an `AggregatorErrorHandler`, individual faulty parts can be
  reported and skipped without aborting the entire aggregation.

## Example

```java

// The feature interface — all assemblers extend the generic Assembler<REQ, V>:
public interface LinkListAssembler extends Assembler<LinkListRequest, LinkList> {}

@AssemblerBinding("linkList")
public class DefaultLinkListAssembler implements LinkListAssembler {

    private final LinkAssemblerDispatcher linkAssembler;
    private final AggregatorErrorHandler errorHandler;

    @Inject
    public DefaultLinkListAssembler(
            LinkAssemblerDispatcher linkAssembler, AggregatorErrorHandler errorHandler) {
        this.linkAssembler = linkAssembler;
        this.errorHandler = errorHandler;
    }

    @Override
    public Optional<LinkList> assemble(LinkListRequest request, @Nullable LinkList previous) {
        Resolver source = request.source();
        LinkListOptions options = request.options();

        Text headline = source.value("sp_headline").asText().translatable();
        String boxType = source.value("sp_linkBoxType").asString(options.box().defaultType());
        List<Link> items = this.assembleItems(source, options.linkOptions());
        if (items.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(LinkList.of("content.linkList", headline, boxType, items));
    }

    private List<Link> assembleItems(Resolver source, LinkOptions options) {

        List<Link> items = new ArrayList<>();
        List<Resolver> resolverList = source.resolveList("sp_link_iterate");
        for (Resolver itemSource : resolverList) {
            try {
                this.linkAssembler.assemble(itemSource, options).ifPresent(items::add);
            } catch (AggregatorException e) {
                this.errorHandler.handle(e);
            }
        }
        return items;
    }
}
```

## Registration via `@AssemblerBinding` and `AssemblerFactory`

Assemblers are registered in the framework via the `@AssemblerBinding` annotation and retrieved via an
`AssemblerFactory`. This makes it possible to look up Assemblers by key and to control
implementation-specific overrides with a priority value:

```java

@AssemblerBinding("link.internal")              // unique key
@AssemblerBinding(value = "link.internal", priority = 100) // overrides built-in assemblers
public class CustomInternalLinkAssembler implements LinkAssembler<Link.InternalLink> {
    // ...
}

// Retrieval via the factory
LinkAssembler assembler = factory.create("link.internal", LinkAssembler.class);
```

Two lookup modes exist:

- `factory.create(key, type)` returns the **single** highest-priority implementation for a key — used
  for genuine single-selection (e.g. picking one link *type*).
- `factory.createChain(key, type)` returns an `AssemblerChain<T>` of **all** implementations for a
  key, ordered by ascending `priority`. Its `assemble(request)` threads the assembled value through
  the chain, passing each assembler's result as the `@Nullable previous` argument of the next (null
  for the first); the built-in (`priority = 0`) produces the base value and higher-priority project
  assemblers enrich or replace it. (A lower-level `fold` is also available for callers that need to
  vary the per-step invocation.) Two `@AssemblerBinding` attributes prune the chain: `chainRoot =
  true` skips all lower-priority assemblers (fresh start), `chainBreak = true` skips all
  higher-priority ones (guaranteed last).

### Context-aware selection (`objectTypes`, `condition`)

Both lookup modes have a context-aware overload — `create(key, type, Resolver context)` and
`createChain(key, type, Resolver context)` — that restricts the candidates to those applicable in
the **current aggregation scope** *before* priority ordering and `chainRoot`/`chainBreak` pruning.
Two `@AssemblerBinding` attributes drive this:

- `objectTypes` — restricts the assembler to the given CMS object types. Empty (the default) means
  it applies to every object type. The object type is derived from the context resolver's current
  scope root (`context.root()`, an `EntityResolver`, via `entityType()`).
- `condition` — a `Class<? extends AssemblerCondition>` for rules a plain `objectTypes` match cannot
  express. The factory instantiates it via dependency injection (so it may declare its own
  constructor dependencies) and calls `appliesTo(source)`. The default `AssemblerCondition.Always`
  always applies and is never instantiated.

Both act as an **AND** — an assembler is eligible only when its `objectTypes` match *and* its
`condition` applies. The plain two-argument `create`/`createChain` pass an empty context, so only
unrestricted assemblers (empty `objectTypes`, default `condition`) match; existing behaviour is
unchanged until an assembler opts in.

Because the scope root switches when a link is followed (`Resolver.resolveLink`), passing the linked
resolver as the context filters by the **linked** object's type. Assemblers that run against a
linked article (a teaser's target, a linked media article, …) are therefore selected by *that*
article's type, not the source's.

```java
// Applies only to the "news" object type; enriches the built-in's result.
@AssemblerBinding(value = "teaser", priority = 100, objectTypes = {"news"})
public final class NewsTeaserAssembler implements TeaserAssembler { /* ... */ }

// A rule objectTypes cannot express, decided at runtime from the resolver.
@AssemblerBinding(value = "teaser", priority = 100, condition = CampaignActiveCondition.class)
public final class CampaignTeaserAssembler implements TeaserAssembler { /* ... */ }
```

A complete example of all patterns (adding new types, chaining, pruning and context restriction) can
be found in [assembler-customization.md](../how-to/assembler-customization.md).

## Distinctions

- An Assembler is **not an Aggregator**, because it does not write to an `OutputNode` but returns a
  domain object.
- An Assembler is **not a Resolver**, because it does not resolve keys but combines already resolved
  values into a finished object.
- Assemblers are usually **stateless** and provided via Dependency Injection.
