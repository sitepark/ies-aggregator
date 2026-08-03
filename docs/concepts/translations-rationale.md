# Translatable Values: Immutability & Identity

> **Type:** Concept
> **Context:** Background to the [Translation Pipeline](../how-to/translations-pipeline.md) – *why*
> the translatable types are built the way they are.

The practical application (collecting, translating, and rendering texts) is described in the
[Translation Pipeline](../how-to/translations-pipeline.md). This document explains the two
design decisions behind it: why the types are **immutable and identity-keyed** and
why they deliberately remain **in the `value` package**.

## Why immutable and identity-keyed?

The translation types are **immutable** and are keyed **by their object identity**:

- The collector returns the same instances that are present in the tree. `Translations.fromIndexed(...)`
  maps exactly those instances to their translation; the writer reads through it during rendering.
- If one worked with copies, the connection between "collected text" and "text in the
  tree" would be lost – the approach only works through identity.
- Two occurrences of the *same* source text at different places in the tree are two separate
  translation slots and may be translated differently.

**The identity lives in the table, not in `equals`.** `Translations` is backed by an
`IdentityHashMap`, which compares its keys by reference regardless of what `equals`/`hashCode` do –
that is guaranteed by the JDK contract of that class. `TranslatableText.equals()` is therefore never
consulted anywhere on the translation path.

That is why the translatable types can define ordinary **value-based** `equals`/`hashCode`, like every
other value object of the `value` package. Value equality is what callers and tests need in order to
compare two instances (`assertThat(actual).isEqualTo(expected)`); it costs the translation mechanism
nothing. Where the *slot* – rather than the value – matters, the distinction is made by reference:
`==`, or `isNotSameAs` in a test.

> **Do not manage translation slots in a value-hashing collection.** A `HashMap`, `HashSet` or
> `Stream.distinct()` over these types collapses value-equal occurrences into one and would silently
> merge their translations. Always go through `Translations` / an `IdentityHashMap`. The collector
> deliberately appends to a plain `ArrayList` for the same reason.

## Deliberate decision: staying in the `value` package

It would have been natural to move the translation types out into a dedicated feature package (`translation`).
**A deliberate decision was made against this** – for two reasons:

1. **The fluent API inevitably couples.** `PlainText.translatable()` returns a `TranslatableText`,
   `PlainUri.translatable()` a `TranslatableUri` (the sealed `Text`/`Uri` interfaces themselves declare
   `Text translatable()` / `Uri translatable()`, which the `Translatable*` types return as `this`).
   Either way, `Text`/`Uri` (in the `value` package) must know these
   types. If one moved them into a `translation` package, `value` would have to access it –
   while `TranslatableUri` conversely holds a `Uri`. Result: a **package cycle**
   `value ↔ translation`.
2. **The `Translations` table belongs here too.** It is built around the identity key
   `TranslatableText`, and `TranslatableUri`/`TranslatableSplitText` render through it
   (`render(Translations)`). If it lived in the rendering or collecting package, a cycle
   `value ↔ output.collect` would arise, since the `TranslatableTextCollector` conversely knows the `value` types.

A sub-package `value.translation` does not help: in Java, parent and child packages are
independent in dependency terms, so the cycle would remain.

**Consequence:** The translation types together with `Translations`/`SourceText` stay with `Text`/`Uri` in the
`value` package. Their additional role as identity keys of the `Translations` table is recorded
**via documentation** (`package-info.java`, this document), not enforced by package boundaries.
