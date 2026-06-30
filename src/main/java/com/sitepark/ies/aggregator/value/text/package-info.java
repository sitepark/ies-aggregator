/**
 * Text values and the translation subsystem.
 *
 * <p>The sealed {@link com.sitepark.ies.aggregator.value.text.Text Text} interface spans a
 * value-equal {@link com.sitepark.ies.aggregator.value.text.PlainText PlainText} and the
 * identity-based {@link com.sitepark.ies.aggregator.value.text.TranslatableText TranslatableText}
 * (and {@link com.sitepark.ies.aggregator.value.text.TranslatableSplitText TranslatableSplitText},
 * sharing the {@link com.sitepark.ies.aggregator.value.text.TranslatableContainer
 * TranslatableContainer} contract).
 *
 * <p>Translatable values are immutable but used <em>by identity</em>: a collector gathers them from
 * the output tree and they serve as the keys of an external {@link
 * com.sitepark.ies.aggregator.value.text.Translations Translations} table, which a
 * translation-aware writer consults while rendering. Because they are identity keys, they
 * intentionally do <em>not</em> define {@code equals}/{@code hashCode}: two distinct occurrences of
 * the same source text may be translated differently. The extraction record {@link
 * com.sitepark.ies.aggregator.value.text.SourceText SourceText} lives here too, next to the
 * identity keys it operates on, to avoid a package cycle with the collecting and rendering packages
 * that depend on them.
 */
@NullMarked
package com.sitepark.ies.aggregator.value.text;

import org.jspecify.annotations.NullMarked;
