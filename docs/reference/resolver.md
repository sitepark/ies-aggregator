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
