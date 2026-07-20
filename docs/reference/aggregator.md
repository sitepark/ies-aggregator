# Aggregator

> **Type:** Reference
> **Role:** Pipeline step that embeds source data into a target data structure.
> **Behavior:** Reads from a Resolver, builds a node of the target structure, writes to an
> OutputNode.

## What is an Aggregator?

An `Aggregator` is the central component of the API. It **composes** part or all of the target
structure from one or more sources. The term "Aggregator" is well established in the CMS and content
domain: a *Content Aggregator* collects content from various sources and provides it in a new,
combined representation – which is exactly what this API does.

An Aggregator differs from an Assembler in two important respects:

1. It writes its result into an **`OutputNode`** (part of a hierarchical target structure),
   instead of returning a typed return value.
2. It can chain **sub-aggregators** and thereby produce nested structures.

Aggregators are the "large" building blocks of an aggregation run. They can be configured,
initialized, and chained into complex pipelines as needed.

## Characteristics

- **Configurable:** Via `Configurable<C extends Configuration>` an Aggregator receives
  type-safe configuration parameters. Runtime-specific options are passed via `OptionsAware`.
- **Lifecycle:** Capability interfaces (`Configurable`, `SubAggregatorAware`, `OptionsAware`)
  are invoked by the orchestrator before `aggregate` – only the hooks that are actually
  implemented take effect.
- **Composable:** Via `SubAggregatorAware`, container aggregators can receive **sub-aggregators**
  and have them produce subordinate substructures. This creates deeply nested output structures.
- **Cross-source:** An Aggregator can also access **linked sources** via Resolvers
  (e.g. referenced objects), so that their data flows directly into the target
  structure.
- **Writing to the output:** An Aggregator does not return a value; instead it populates the
  provided `OutputNode`.
- **Uses Assemblers:** To produce typed values, Aggregators typically rely on
  Assemblers.

## Interface

```java
public interface Aggregator {

    void aggregate(Resolver source, OutputNode output) throws AggregatorException;
}
```

The interface is deliberately kept lean. Lifecycle capabilities are registered via dedicated
**capability interfaces** that an Aggregator optionally implements:

| Interface                               | Purpose                                                           |
|-----------------------------------------|-------------------------------------------------------------------|
| `Configurable<C extends Configuration>` | Receives typed configuration before the first aggregation run     |
| `SubAggregatorAware`                    | Receives named sub-aggregators for hierarchical composition       |
| `OptionsAware<O extends Options>`       | Receives runtime-specific options per aggregation run             |

The orchestrator invokes these hooks — if implemented — in the order
`Configurable → SubAggregatorAware → OptionsAware`, before `aggregate` is executed.

## RootAggregator

A `RootAggregator` orchestrates an ordered list of `Aggregator` instances. It invokes each
Aggregator in turn and writes all results into the same `OutputObject` root node:

```java
RootAggregator root = new RootAggregator(List.of(
        metaAggregator,
        linkListAggregator
));
OutputObject result = root.aggregate(resolver);
```

## Example

```java
public class LinkListAggregator implements Aggregator, OptionsAware<LinkListOptions> {

    private final AssemblerFactory assemblerFactory;
    private LinkListOptions options;

    @Inject
    public LinkListAggregator(AssemblerFactory assemblerFactory) {
        this.assemblerFactory = assemblerFactory;
    }

    @Override
    public void setOptions(LinkListOptions options) {
        this.options = options;
    }

    @Override
    public void aggregate(Resolver source, OutputNode output) throws AggregatorException {
        output.put("collector", "manually");
        LinkListAssembler linkListAssembler =
                this.assemblerFactory.create("linkList", LinkListAssembler.class, source);
        linkListAssembler
                .assemble(LinkListRequest.of(source, this.options), null)
                .ifPresent(linkList -> output.put("model", linkList));
    }
}
```

## Distinctions

- An Aggregator is **not an Assembler**: it does not return a value but writes into a
  target structure.
- An Aggregator is **not a Resolver**: it does not resolve keys but reads from a
  Resolver and produces output.
- An Aggregator is **not an Aggregator in the sense of the Enterprise Integration Patterns** – there
  the term has a different meaning (multiple messages → one). Here we follow the meaning of
  "Content Aggregator" established in the CMS/content domain.
