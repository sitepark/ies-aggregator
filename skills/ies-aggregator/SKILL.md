---
name: ies-aggregator
description: The IES Aggregator API (com.sitepark.ies:ies-aggregator) - Resolver, Assembler, @AssemblerBinding, AssemblerFactory and chains, Aggregator, OutputNode/Component, Visitor, translatable texts, the ports (Channel, MediaProvider, ImageScaler, RootResolverFactory, ...), config.pkl registration, the IES module jar and the scenario testkit. Use when writing, extending, registering or testing an aggregator or assembler in any project that depends on ies-aggregator, or when asked what the API offers.
---

# IES Aggregator API

The API turns CMS data into prepared data structures (for a PHP frontend, a search index, JSON).
This plugin carries the whole `ies-aggregator` repository: the documentation **and** the source.
Read them instead of guessing a signature:

- Documentation: `${CLAUDE_PLUGIN_ROOT}/README.md` and `${CLAUDE_PLUGIN_ROOT}/docs/`
- Source: `${CLAUDE_PLUGIN_ROOT}/src/main/java/com/sitepark/ies/aggregator/`
- Module configuration schema: `${CLAUDE_PLUGIN_ROOT}/src/main/resources/aggregatorConfig.pkl`

The plugin follows the repository's main branch. The project you work in pins a version in its
`pom.xml`; when a type is missing there, compare with the sources jar of that version in the local
Maven repository before you conclude it does not exist.

## The roles

| Role | Type | Job | Read |
|---|---|---|---|
| Resolver | `resolver.Resolver`, `EntityResolver`, `GroupResolver` | read-only view of one CMS object; `value(field)`, `resolve(key)`, `resolveLink(key)`, `root()`, `isEmpty()` | `docs/reference/resolver.md` |
| Assembler | `Assembler<REQ, V>` | builds one typed value object from a request; `assemble(request, @Nullable previous)` returns `Optional<V>` | `docs/reference/assembler.md` |
| Aggregator | `Aggregator` | `aggregate(Resolver source, OutputNode output)`; composes the target structure | `docs/reference/aggregator.md` |
| OutputNode | `output.OutputObject`, `OutputList`, `Component` | the mutable target tree an aggregator writes into | `docs/reference/output-node.md` |
| Visitor | `output.OutputVisitor`, `DomainObjectMapper` | serializes or evaluates the tree; output annotations `@OutputProperty`, `@OutputUnwrapped`, `@OutputType`, `@OutputKeepIfEmpty` | `docs/reference/visitor.md` |

Values written into the tree live in `value/`: `text.Text` (`PlainText`, `TranslatableText`),
`uri.Uri`, `media.Media` (`Image`, `Document`, `Audio`, `Video`, …), `ResolvedValue`,
`AccessRestriction`, `Publication`.

## The ports

Ports are interfaces the IES implements; inject them with `@Inject` in the constructor of an
aggregator, assembler or condition. All live in `port/` (one line of Javadoc each explains it):

| Port | For |
|---|---|
| `AssemblerFactory` | look up an assembler by key: `create(key, type, source)` (single winner) or `createChain(key, type, source)` (all, ordered by priority) |
| `RootResolverFactory` (`resolver/`) | read an object that is not linked from the current one, by id or anchor |
| `Channel`, `ChannelProvider` | URLs of objects in a publication channel, publication state |
| `MediaProvider`, `ImageScaler`, `VariantConfigProvider`, `ZipArchives` | media assets, image scaling, named image variants, zip downloads |
| `GlobalConfigProvider`, `ObjectTypeConfigProvider` | the installation's configuration |
| `HomepageProvider`, `CategoryRootProvider`, `ObjectTypeEntities`, `ContainerHierarchy` | finding objects and structures |
| `ModuleDependencies`, `PublishedLinks`, `Regeneration`, `ResourceState`, `IdSequence` | talking to the publisher about the resource being generated |
| `Geocoder`, `DataSigner`, `ContentNotificationRecipientProvider` | special services |

Before relying on a port method for the first time, read its Javadoc: a port is a contract, and
what a method promises is stated there, not in its name.

## Rules that are easy to get wrong

- **`ies-aggregator` is `provided`.** The IES brings it at runtime. Never shade it.
- **An aggregator never holds an assembler.** It holds the `AssemblerFactory` and looks the
  assembler up by key on every run, passing `source` as context. That indirection is what makes an
  assembler replaceable.
- **Overriding = same key, higher `priority`** in `@AssemblerBinding`. `chainRoot = true` drops
  everything with lower priority, `chainBreak = true` everything higher. `objectTypes` and
  `condition` (an `AssemblerCondition`, built by DI) restrict an assembler to a scope; both must
  match. Patterns: `docs/how-to/assembler-customization.md`.
- **In a chain, `previous` may be `null`** (first in the chain) and an assembler that has nothing to
  do returns `previous` unchanged.
- **Assemblers and aggregators are stateless.** The factory creates a fresh instance per call.
- **Registration needs no code.** Assemblers are found through `@AssemblerBinding`. A section type
  or object type is registered in the jar's `config.pkl`, which amends
  `modulepath:/ies-aggregator/aggregatorConfig.pkl` (keys and their meaning: that file). Keys must
  not contain dots.
- **Do not bind a port the IES already binds** in a Guice module of your own: the module will not
  start (`BindingAlreadySet`). A Guice module is only needed for a port the IES does not know.
- **A missing object is not an error.** Resolvers and `RootResolverFactory` return empty objects,
  never `null` and never an exception; check `isEmpty()`.

## Tasks

| Task | Read |
|---|---|
| Set up a project that builds an installable IES module jar (`ies-module.build.toml`, shading, `ies:install-module`) | `docs/how-to/aggregator-plugin-project.md` |
| Add a new variant to an assembler family, override or enrich an existing assembler, add fields to a model | `docs/how-to/assembler-customization.md` |
| Deliver the same structure in several languages | `docs/how-to/translations-pipeline.md`, `docs/concepts/translations-rationale.md` |
| Control what the serialized output looks like | `docs/reference/visitor.md` |

## Testing with the testkit

`com.sitepark.ies:ies-aggregator-testkit` (scope `test`, repository
`github.com/sitepark/ies-aggregator-testkit`) loads a JSON scenario as an in-memory CMS, wires the
real assembler chain through the ports and compares the result with
`<name>.expected.json`. A project writes one base class per kind of aggregation (extending
`ScenarioTest`) and then adds cases as file pairs, without code. Its README documents the scenario
format and which port stands in for what; it is not part of this plugin, so read it from the
testkit's sources jar or repository. The harness is deliberately no more generous than
production - when something passes there and fails in the IES, suspect the harness first.
