/**
 * Media assets and their descriptive metadata.
 *
 * <p>{@link com.sitepark.ies.aggregator.value.media.Media Media} is sealed over the concrete kinds
 * {@link com.sitepark.ies.aggregator.value.media.Image Image}, {@link
 * com.sitepark.ies.aggregator.value.media.Document Document}, {@link
 * com.sitepark.ies.aggregator.value.media.Audio Audio} and {@link
 * com.sitepark.ies.aggregator.value.media.Video Video}. Descriptive data is itself a sealed family
 * {@link com.sitepark.ies.aggregator.value.media.Metadata Metadata}: {@link
 * com.sitepark.ies.aggregator.value.media.GenericMetadata GenericMetadata} for the shared fields
 * and {@link com.sitepark.ies.aggregator.value.media.ImageMetadata ImageMetadata}, which adds an
 * image {@link com.sitepark.ies.aggregator.value.media.FocalPoint FocalPoint}.
 */
@NullMarked
package com.sitepark.ies.aggregator.value.media;

import org.jspecify.annotations.NullMarked;
