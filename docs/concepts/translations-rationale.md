# Translatable Values: Immutability & Identity

> **Type:** Concept
> **Context:** Background to the [Translation Pipeline](../how-to/translations-pipeline.md) – *why*
> the translatable types are built the way they are.

The practical application (collecting, translating, and rendering texts) is described in the
[Translation Pipeline](../how-to/translations-pipeline.md). This document explains the two
design decisions behind it: why the types are **immutable and identity-based** and
why they deliberately remain **in the `value` package**.

## Why immutable and identity-based?

The translation types are **immutable** and are used **by their object identity** –
not by their value:

- The collector returns the same instances that are present in the tree. `Translations.fromIndexed(...)`
  maps exactly those instances to their translation; the writer reads through it during rendering.
- If one worked with copies, the connection between "collected text" and "text in the
  tree" would be lost – the approach only works through identity.

That is why `TranslatableText`, `TranslatableUri`, and `TranslatableSplitText` define **no**
`equals`/`hashCode`: they are identity keys of the `Translations` table (an
`IdentityHashMap`). Value equality would be misleading – two "equal" source texts at different
places may be translated differently – and dangerous as an ordinary HashMap key.
Reference identity is the correct semantics here.

This distinguishes them from the **value-equal** objects `Text` and `Uri`, which possess value `equals`/
`hashCode`. Both categories are immutable; the difference lies in the
equality semantics (value vs. identity). This distinction is recorded in the
[`package-info.java`](../../src/main/java/com/sitepark/ies/aggregator/value/package-info.java) of the
`value` package.

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
`value` package. The domain distinction "value-equal object vs. identity-based value"
is made **via documentation** (`package-info.java`, this document), not enforced by package
boundaries.
