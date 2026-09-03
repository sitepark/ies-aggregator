/**
 * Media assets and their descriptive metadata.
 *
 * <p>A media asset is always used through a CMS object (page/article) — either a media object whose
 * content is the asset, or an object that embeds it; {@link
 * com.sitepark.ies.aggregator.value.media.Media#objectId() Media.objectId()} names that object.
 *
 * <p>{@link com.sitepark.ies.aggregator.value.media.Media Media} is sealed over the concrete kinds
 * {@link com.sitepark.ies.aggregator.value.media.Image Image}, {@link
 * com.sitepark.ies.aggregator.value.media.Document Document}, {@link
 * com.sitepark.ies.aggregator.value.media.Audio Audio} and {@link
 * com.sitepark.ies.aggregator.value.media.Video Video}, plus {@link
 * com.sitepark.ies.aggregator.value.media.EmptyMedia EmptyMedia} — the empty media asset returned by
 * {@link com.sitepark.ies.aggregator.value.media.Media#empty() Media.empty()}. Descriptive data is
 * itself a sealed family
 * {@link com.sitepark.ies.aggregator.value.media.Metadata Metadata}: {@link
 * com.sitepark.ies.aggregator.value.media.GenericMetadata GenericMetadata} for the shared fields
 * and {@link com.sitepark.ies.aggregator.value.media.ImageMetadata ImageMetadata}, which adds an
 * image {@link com.sitepark.ies.aggregator.value.media.FocalPoint FocalPoint}.
 *
 * <p>The focal point is descriptive data an editor states, so it belongs to the metadata. The {@link
 * com.sitepark.ies.aggregator.value.media.ColorSwatch ColorSwatch} of an {@link
 * com.sitepark.ies.aggregator.value.media.Image Image} is not: like the pixel dimensions it is
 * derived from the binary itself and therefore sits on the asset.
 *
 * <p>The {@link com.sitepark.ies.aggregator.value.media.Origin Origin} sits on the asset too, but on
 * {@link com.sitepark.ies.aggregator.value.media.Media Media} rather than on one kind of it: where
 * an asset was synchronised from is a property of the binary record, and a document imported from a
 * media-management system has a provenance just as an image does.
 */
@NullMarked
package com.sitepark.ies.aggregator.value.media;

import org.jspecify.annotations.NullMarked;
