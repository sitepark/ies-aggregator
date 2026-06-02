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
- **Reusable:** Multiple aggregators can use the same Assembler.
- **Composable:** Assemblers may use other Assemblers (e.g. `LinkListAssembler` uses the
  `LinkAssembler` for each element).
- **Does not write to the OutputNode:** Assemblers do not know the OutputNode - they only return the
  finished object. What happens with it is decided by the caller (usually an Aggregator).
- **Fault tolerance possible:** Via an `AggregatorErrorHandler`, individual faulty parts can be
  reported and skipped without aborting the entire aggregation.

## Example

```java

@Assembler("linkList")
public class LinkListAssembler {

    private final LinkAssemblerDispatcher linkAssembler;
    private final AggregatorErrorHandler errorHandler;

    @Inject
    public LinkListAssembler(
            LinkAssemblerDispatcher linkAssembler, AggregatorErrorHandler errorHandler) {
        this.linkAssembler = linkAssembler;
        this.errorHandler = errorHandler;
    }

    public Optional<LinkList> assemble(Resolver source, LinkListOptions options) {

        Text headline = source.value("sp_headline").asText().translatable();
        String boxType = source.value("sp_linkBoxType").asString(options.box().defaultType());
        List<Link> items = this.assembleItems(source, options.linkOptions());
        if (items.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new LinkList("content.linkList", headline, boxType, items));
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

## Registration via `@Assembler` and `AssemblerFactory`

Assemblers are registered in the framework via the `@Assembler` annotation and retrieved via an
`AssemblerFactory`. This makes it possible to look up Assemblers by key and to control
implementation-specific overrides with a priority value:

```java

@Assembler("link.internal")              // unique key
@Assembler(value = "link.internal", priority = 100) // overrides built-in assemblers
public class CustomInternalLinkAssembler {
    // ...
}

// Retrieval via the factory
LinkAssembler assembler = factory.create("link.internal", LinkAssembler.class);
```

When there are multiple implementations of the same key, the implementation with the highest
`priority` value wins - this allows project-specific libraries to replace built-in assemblers without
changing the aggregation code.

A complete example of this pattern (adding new types, overriding existing ones) can be found
in [assembler-customization.md](../how-to/assembler-customization.md).

## Distinctions

- An Assembler is **not an Aggregator**, because it does not write to an `OutputNode` but returns a
  domain object.
- An Assembler is **not a Resolver**, because it does not resolve keys but combines already resolved
  values into a finished object.
- Assemblers are usually **stateless** and provided via Dependency Injection.
