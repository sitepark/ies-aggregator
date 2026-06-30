/**
 * Typed values that aggregators write into the output tree.
 *
 * <p>This package holds the cross-cutting value objects with value-based {@code equals}/{@code
 * hashCode}: {@link com.sitepark.ies.aggregator.value.ResolvedValue ResolvedValue}, {@link
 * com.sitepark.ies.aggregator.value.Channel Channel}, {@link
 * com.sitepark.ies.aggregator.value.Publication Publication} and the {@link
 * com.sitepark.ies.aggregator.value.NamedEnum NamedEnum} mixin.
 *
 * <p>The larger value families live in dedicated sub-packages:
 *
 * <ul>
 *   <li>{@link com.sitepark.ies.aggregator.value.text text} — the sealed {@link
 *       com.sitepark.ies.aggregator.value.text.Text Text} interface and the translation subsystem
 *       (including the identity-based {@link
 *       com.sitepark.ies.aggregator.value.text.TranslatableText TranslatableText} and {@link
 *       com.sitepark.ies.aggregator.value.text.Translations Translations}).
 *   <li>{@link com.sitepark.ies.aggregator.value.uri uri} — the sealed {@link
 *       com.sitepark.ies.aggregator.value.uri.Uri Uri} interface ({@link
 *       com.sitepark.ies.aggregator.value.uri.PlainUri PlainUri} / {@link
 *       com.sitepark.ies.aggregator.value.uri.TranslatableUri TranslatableUri}).
 *   <li>{@link com.sitepark.ies.aggregator.value.media media} — the sealed {@link
 *       com.sitepark.ies.aggregator.value.media.Media Media} family and its metadata.
 * </ul>
 */
@NullMarked
package com.sitepark.ies.aggregator.value;

import org.jspecify.annotations.NullMarked;
