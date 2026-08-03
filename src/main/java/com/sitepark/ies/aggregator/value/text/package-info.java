/**
 * Text values and the translation subsystem.
 *
 * <p>The sealed {@link com.sitepark.ies.aggregator.value.text.Text Text} interface spans {@link
 * com.sitepark.ies.aggregator.value.text.PlainText PlainText} and {@link
 * com.sitepark.ies.aggregator.value.text.TranslatableText TranslatableText} (and {@link
 * com.sitepark.ies.aggregator.value.text.TranslatableSplitText TranslatableSplitText}, sharing the
 * {@link com.sitepark.ies.aggregator.value.text.TranslatableContainer TranslatableContainer}
 * contract). All of them are immutable and define value-based {@code equals}/{@code hashCode}.
 *
 * <p>Translatable values are additionally used <em>as identity keys</em>: a collector gathers them
 * from the output tree and they serve as the keys of an external {@link
 * com.sitepark.ies.aggregator.value.text.Translations Translations} table, which a
 * translation-aware writer consults while rendering. That keying is by reference and independent of
 * their {@code equals} (see {@link com.sitepark.ies.aggregator.value.text.TranslatableText
 * TranslatableText}). The extraction record {@link
 * com.sitepark.ies.aggregator.value.text.SourceText SourceText} lives here too, next to the identity
 * keys it operates on, to avoid a package cycle with the collecting and rendering packages that
 * depend on them.
 */
@NullMarked
package com.sitepark.ies.aggregator.value.text;

import org.jspecify.annotations.NullMarked;
