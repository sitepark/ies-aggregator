package com.sitepark.ies.aggregator;

import org.jspecify.annotations.Nullable;

/**
 * A section type of a {@link Step} that is placed in the current channel: its configuration, and
 * the aggregator the platform created for it - configured, with its options and, for a container,
 * its children.
 *
 * @param type the part of the section type's configuration needed to place it
 * @param aggregator the aggregator, or {@code null} if the section type has only its SPML
 *     aggregator; a page with such a section type cannot be built in Java
 */
public record PlacedSectionType(SubSectionType type, @Nullable Aggregator aggregator) {}
