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
  empty `ResolvedValue` is returned (`Resolver.EMPTY`).

## Interface

```java
public interface Resolver {

    Resolver EMPTY = new EmptyResolver();

    boolean isEmpty();

    Resolver resolve(String key);              // sub-resolver

    List<Resolver> resolveList(String key);    // list of sub-resolvers

    ResolvedValue value(String key);           // typable value

    // Returns the first non-empty value from the given keys
    default ResolvedValue coalesce(String... keys);
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

}
```

## Distinctions

- A resolver is **not** an assembler – it does not provide finished domain objects, only raw
  (typed-readable) values and sub-resolvers.
- A resolver is **not** an aggregator – it produces no output, but serves as a read source.
- In method signatures, a resolver typically appears as the parameter `source` – its
  **role** is that of a data source, its **capability** is that of a resolver.
