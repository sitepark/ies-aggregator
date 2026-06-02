/**
 * Typed values that aggregators write into the output tree.
 *
 * <p>This package holds two deliberately different categories of types:
 *
 * <ul>
 *   <li><b>Value-equal value objects</b> with value-based {@code equals}/{@code hashCode}: {@link
 *       com.sitepark.ies.aggregator.value.PlainText PlainText}, {@link
 *       com.sitepark.ies.aggregator.value.PlainUri PlainUri}, {@link
 *       com.sitepark.ies.aggregator.value.ResolvedValue ResolvedValue}, {@link
 *       com.sitepark.ies.aggregator.value.Channel Channel} and {@link
 *       com.sitepark.ies.aggregator.value.Publication Publication}. Two instances with equal content
 *       are equal.
 *   <li><b>Identity-based translatable values</b>: {@link
 *       com.sitepark.ies.aggregator.value.TranslatableText TranslatableText}, {@link
 *       com.sitepark.ies.aggregator.value.TranslatableUri TranslatableUri} and {@link
 *       com.sitepark.ies.aggregator.value.TranslatableSplitText TranslatableSplitText} (sharing the
 *       {@link com.sitepark.ies.aggregator.value.TranslatableContainer TranslatableContainer}
 *       contract). These are immutable too, but they are used <em>by identity</em>: a {@code
 *       TranslatableTextCollector} collects them from the output tree and they serve as the keys of
 *       an external {@link com.sitepark.ies.aggregator.value.Translations Translations} table, which
 *       a translation-aware writer consults while rendering. Because they are identity keys, they
 *       intentionally do <em>not</em> define {@code equals}/{@code hashCode}: two distinct
 *       occurrences of the same source text may be translated differently.
 * </ul>
 *
 * <p>The sealed {@link com.sitepark.ies.aggregator.value.Text Text} and {@link
 * com.sitepark.ies.aggregator.value.Uri Uri} interfaces each span both kinds ({@code PlainText} /
 * {@code TranslatableText} and {@code PlainUri} / {@code TranslatableUri} respectively), so a field
 * can accept either and let the aggregator decide per value whether the text or URI should be
 * translatable.
 *
 * <p>The translation table {@link com.sitepark.ies.aggregator.value.Translations Translations} and
 * the extraction record {@link com.sitepark.ies.aggregator.value.SourceText SourceText} also live
 * here, next to the identity keys they operate on, to avoid a package cycle with the collecting and
 * rendering packages that depend on them.
 */
package com.sitepark.ies.aggregator.value;
