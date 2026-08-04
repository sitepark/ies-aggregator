# Aggregator API

The Aggregator API turns CMS source data into new, prepared data structures – for example, for
delivery to frontends, search indexes, or other consumers.

The API is clearly layered. Each role has a well-defined responsibility:

| Role                                        | Purpose                                                 |
|---------------------------------------------|---------------------------------------------------------|
| [**Resolver**](docs/reference/resolver.md)            | Reads raw source data (Source)                          |
| [**Assembler**](docs/reference/assembler.md)          | Builds typed domain objects from a Resolver             |
| [**Aggregator**](docs/reference/aggregator.md)        | Composes the target structure into an OutputNode        |
| [**OutputNode**](docs/reference/output-node.md)       | Hierarchical target data structure (Target)             |
| [**Visitor**](docs/reference/visitor.md)              | Traverses an OutputNode (serialization, analyses)       |

## Data flow

```mermaid
flowchart LR
    Source[("Source data<br/>(CMS, Map, JSON, ...)")]
    Resolver["Resolver<br/><i>read-only view</i>"]
    Assembler["Assembler<br/><i>optional</i>"]
    Domain[["Domain objects<br/>(Link, Text, ...)"]]
    Aggregator["Aggregator"]
    Output[("OutputNode<br/><i>target structure</i>")]
    Visitor["Visitor"]
    Target[["PHP / JSON / Map /<br/>TranslatableTexts ..."]]

    Source --> Resolver
    Resolver --> Assembler
    Assembler --> Domain
    Resolver --> Aggregator
    Domain --> Aggregator
    Aggregator --> Output
    Output --> Visitor
    Visitor --> Target
```

## When do I use what?

| Question                                                              | Answer                                     |
|-----------------------------------------------------------------------|--------------------------------------------|
| How do I get individual values from a source?                         | **Resolver**                               |
| How do I build a typed value object (e.g. `Link`)?                    | **Assembler**                              |
| How do I write results into the target structure?                     | **Aggregator** + **OutputNode**            |
| How do I structure the data hierarchy to be generated?                | **OutputNode** (optionally with sub-aggregators) |
| How do I serialize the result (PHP, JSON, Map) or evaluate it?        | **Visitor**                                |
| How do I deliver the same structure in multiple languages?            | [**Translations**](docs/how-to/translations-pipeline.md)  |

## How it fits together

In a typical aggregation, all roles work together:

```java
public class LinkListAggregator implements Aggregator, OptionsAware<LinkListOptions> {

  private final AssemblerFactory assemblerFactory;
  private LinkListOptions options;

  public LinkListAggregator(AssemblerFactory assemblerFactory) {
    this.assemblerFactory = assemblerFactory;
  }

  @Override
  public void setOptions(LinkListOptions options) {
    this.options = options;
  }

  @Override
  public void aggregate(Resolver source, OutputNode output) {
    //                  ^^^^^^^^         ^^^^^^^^^^
    //                  RESOLVER         OUTPUTNODE
    //                  (Source)         (Target)

    LinkListAssembler linkListAssembler =
        this.assemblerFactory.create("linkList", LinkListAssembler.class, source);
    //       ^^^^^^^^^^^^^^^^
    //       ASSEMBLERFACTORY resolves the key to an implementation and returns a fresh instance

    linkListAssembler.assemble(LinkListRequest.of(source, this.options), null)
        //  ^^^^^^^^^
        //  ASSEMBLER returns a typed domain object
        .ifPresent(linkList -> output.put("linkList", linkList));
    //                         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    //                         AGGREGATOR writes into the OutputNode
  }
}
```

An Aggregator never receives an Assembler directly — it receives the
[`AssemblerFactory`](docs/reference/assembler.md#registration-via-assemblerbinding-and-assemblerfactory)
and looks the Assembler up by key per aggregation run. That indirection is what makes an Assembler
replaceable: a customer-specific implementation registers under the same key with a higher priority
and wins, without the Aggregator changing. Passing `source` along lets the factory additionally
restrict the candidates to those applicable in the current scope. Where several implementations are
meant to build on each other instead of overriding, `createChain(key, type, source)` returns the
whole chain (see [Assembler customization](docs/how-to/assembler-customization.md)).

Assemblers are optional: for simple fields, an Aggregator can read directly from the Resolver and
write to the OutputNode. Only when constructing a value involves multiple steps (several fields,
derived values, business rules, external resolutions) is a dedicated Assembler worthwhile.

## Documentation at a glance

The documentation is organized by genre:

| Genre         | Directory        | Content                                                                                                            |
|---------------|------------------|---------------------------------------------------------------------------------------------------------------------|
| **Reference** | [`docs/reference/`](docs/reference/) | Role reference: [Resolver](docs/reference/resolver.md), [Assembler](docs/reference/assembler.md), [Aggregator](docs/reference/aggregator.md), [OutputNode](docs/reference/output-node.md), [Visitor](docs/reference/visitor.md) |
| **How-To**    | [`docs/how-to/`](docs/how-to/)       | Task-oriented: [Aggregator plugin project](docs/how-to/aggregator-plugin-project.md), [Assembler customization](docs/how-to/assembler-customization.md), [Translations pipeline](docs/how-to/translations-pipeline.md) |
| **Concept**   | [`docs/concepts/`](docs/concepts/)   | Background & design decisions: [Translatable values: immutability & identity](docs/concepts/translations-rationale.md) |
