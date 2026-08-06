# Resolver

> **Type:** Reference
> **Role:** Read interface to a data source (Source).
> **Behavior:** Resolves keys to values or to further (nested) resolvers.

## What is a Resolver?

A `Resolver` is a **read-only view** of a single source – typically a
CMS object, but it can also be a Map, a JSON structure, or any other
data representation. The term "Resolver" describes its behavior: it **resolves
keys** – either to a value or to a further, subordinate source (sub-resolver).

A resolver knows nothing about the underlying storage technology. Aggregators and assemblers
**never** work directly with the storage, but always through resolvers. This allows
sources to be swapped out (real CMS, mocks for testing, external data) without changes to the
aggregation code.

## Characteristics

- **Read-only:** A resolver does not write; it only answers read queries.
- **Key-based:** Access happens via `String` keys, e.g. field names such as `"sp_headline"`.
- **Hierarchical:** Sub-resolvers are returned via `resolve(key)` or `resolveList(key)` –
  a resolver returns resolvers again. This fractal structure makes it possible to navigate arbitrarily deep into the
  source.
- **Type-safe reading via `ResolvedValue`:** Values are returned via `value(key)` as a `ResolvedValue`
  and can be converted in a typed manner (`asText`, `asEnum`, `asInt`, …).
- **Null-safe:** Even for missing keys, an empty (but valid) resolver or an
  empty `ResolvedValue` is returned (`Resolver.empty()`).
- **Scope-aware / navigable:** Following a *link* via `resolve(key)` crosses a scope boundary into
  the linked object. `root()` returns to the top of the current scope, `globalRoot()` to the top of
  the outermost scope, and `path()` (a [`ResolverPath`](#navigation-and-scopes)) records the full,
  step-by-step navigation history across every scope boundary crossed.

## Interface

```java
public interface Resolver {

    // Null-object factories
    static Resolver empty();                   // shared, self-rooted empty resolver
    static Resolver empty(ResolverPath path);  // empty resolver keeping the surrounding path

    boolean isEmpty();

    Resolver resolve(String key);              // sub-resolver (may cross a link/scope boundary)

    List<Resolver> resolveList(String key);    // list of sub-resolvers

    ResolvedValue value(String key);           // typable value

    ResolverPath path();                       // full navigation path (global root → here)

    default Resolver root();                   // root of the current scope
    default Resolver globalRoot();             // root of the outermost scope

    // Returns the first non-empty value from the given keys
    default ResolvedValue coalesce(String... keys);
}
```

## Entities and groups

When the current scope is a CMS entity, the resolver is an `EntityResolver`; for a group it is a
`GroupResolver`. Both split their additional API along one line: **master data lives in a descriptor,
navigation stays on the resolver.**

```java
public interface EntityResolver extends Resolver {
    EntityDescriptor entity();                 // master data of the entity read from
    @Nullable GroupResolver parentGroup();     // navigation
    List<GroupResolver> parentGroupPath();
}

public interface EntityDescriptor {
    static EntityDescriptor empty();

    int id();
    String type();
    String name();
    String anchor();
    Revision created();                        // Revision: at() + by(), see below
    Revision changed();
}

public interface GroupResolver extends EntityResolver {
    @Override
    GroupDescriptor entity();                  // narrowed: adds the group-specific fields

    List<GroupResolver> subGroups();            // navigation
    List<EntityResolver> entities();
    List<EntityResolver> children();

    default boolean isPathRoot();
}
```

A descriptor is a **view, not a snapshot**: obtaining it resolves nothing, every field is read on
access. That matters for values that are expensive to produce — `Revision.by()` yields an `Editor`
whose `id()` is cheap while `name()` is resolved only when it is actually read, so a template that
never prints the editor never pays for the lookup. Missing data is never `null` at the descriptor
level: an empty resolver returns `EntityDescriptor.empty()` / `GroupDescriptor.empty()`, whose fields
are neutral and whose revisions are `Revision.empty()`.

New master data fields are added to the descriptor, not as new methods on the resolver.

## Navigation and scopes

A resolver is a node in a tree of source data. Chaining `resolve(key)` navigates deeper; a field
that references another CMS object is a **link**, and resolving it crosses a *scope boundary* — the
returned resolver is the root of the linked object, and subsequent navigation is relative to it.

- `root()` returns to the top of the **current** scope (the most recently entered linked object).
- `globalRoot()` returns to the top of the **outermost** scope (the object the call chain started
  with), regardless of how many link boundaries were crossed.
- `path()` returns a `ResolverPath`: the ordered segments from the global root to this resolver. It
  never resets — it keeps growing across every scope boundary — and is the underlying source for
  both `root()` and `globalRoot()`.

A resolver may also start a **fresh root**: crossing into a standalone, top-level object that
becomes the new `globalRoot()` (not just the current-scope `root()`, as a plain link does), while
`path()` keeps growing so the navigation history remains intact. This is the `enterRoot` case on
`ResolverPath`, as opposed to the `enterScope` link boundary above.

An empty resolver from a failed lookup still carries the surrounding `path()`, so callers can
navigate back up the tree even after a missing key (`Resolver.empty(path)`).

## Obtaining a root resolver

There are three ways to get hold of a resolver:

1. The `source` resolver the framework hands to an [Aggregator](aggregator.md) or
   [Assembler](assembler.md) — the object currently being aggregated.
2. `resolve(key)` / `resolveLink(key)` — navigating along the fields of that object.
3. The **`RootResolverFactory`** — for an object that is *not* reachable from the current one,
   e.g. a portal page configured by anchor. It is also what the IES runtime uses to start a
   generation run.

It is injected like any other dependency (`Channel`, `MediaProvider`, …) via `@Inject`:

```java
public interface RootResolverFactory {

    EntityResolver createByEntityId(int id);              // address by entity id
    EntityResolver createByEntityAnchor(String anchor);   // address by anchor, e.g. "hauptseite"
    EntityResolver createByEntityId(ResolverPath path, int id);  // new root inside an existing path

    GroupResolver createByGroupId(int id);                // the same for groups
    GroupResolver createByGroupAnchor(String anchor);
    GroupResolver createByGroupId(ResolverPath path, int id);
}
```

Its contract:

- **Fresh instance per call:** every `createBy…` call produces a new resolver, so mutable
  aggregator state does not leak between generations.
- **Fresh root:** the returned resolver starts a brand-new resolver tree — it is its own `root()` and
  `globalRoot()`, and its `path()` begins fresh. It inherits neither the scope nor the path of the
  caller, so navigating up from it never leads back into the calling object.
- **Or a root within an existing path:** the overloads taking a `ResolverPath` make the created
  resolver a new `globalRoot()` too, but append it to the given path (`enterRoot`) so the
  navigation history is preserved.
- **Typed target:** `createByEntity…` returns an `EntityResolver` (master data via `entity()`),
  `createByGroup…` a `GroupResolver`.
- **Null-safe:** an unknown id or anchor is a normal case (deleted object, misspelled anchor in an
  editorial field), not an error — never `null`, never an exception. Test with `isEmpty()`. The
  standalone methods return an empty, self-rooted resolver of the requested type
  (`EntityResolver.emptyRoot()` resp. `GroupResolver.emptyRoot()`); the `ResolverPath` overloads
  return an empty resolver carrying the given path (`EntityResolver.empty(path)` resp.
  `GroupResolver.empty(path)`), so the navigation history stays intact.
- **Anchors over ids:** an anchor is the stable, human-readable alias of an object. Prefer it for
  objects referenced from configuration, because it survives copying and re-importing while an id
  does not.

```java
// A portal page configured by anchor, not reachable via a link of the current object
EntityResolver portal = this.rootResolverFactory.createByEntityAnchor("hauptseite");
if (!portal.isEmpty()) {
    Text title = portal.value("sp_title").asText();
}
```

## Example

```java
public void example() {
    // Simple value access with default
    Text headline = resolver.value("sp_headline").asText();

    // Enum conversion
    MyEnum enumValue = resolver.value("sp_enumValue").asEnum(MyEnum.class, MyEnum.NONE);

    // Hierarchical navigation
    Text subHeadline = resolver.resolve("sub").value("sp_headline").asText();

    // List processing via sub-resolvers
    List<Resolver> resolverList = source.resolveList("sp_link_iterate");
    for (Resolver itemSource : resolverList) {
        // Individual resolvers
    }

    // Fallback across multiple keys – the first non-empty value wins
    Text title = resolver.coalesce("sp_title", "sp_headline").asText();

    // Link navigation and returning to a scope root
    Resolver author = resolver.resolve("author");        // crosses a link boundary into Author
    Text city = author.resolve("address").root()         // → root of the Author scope
                      .value("city").asText();
    Resolver top = author.globalRoot();                  // → back to the original object
}
```

## Distinctions

- A resolver is **not** an assembler – it does not provide finished domain objects, only raw
  (typed-readable) values and sub-resolvers.
- A resolver is **not** an aggregator – it produces no output, but serves as a read source.
- In method signatures, a resolver typically appears as the parameter `source` – its
  **role** is that of a data source, its **capability** is that of a resolver.
- A root resolver from the [`RootResolverFactory`](#obtaining-a-root-resolver) is **not** a
  sub-resolver – it inherits neither the scope nor the path of the code that requested it.
