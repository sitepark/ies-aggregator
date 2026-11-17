package com.sitepark.ies.aggregator.container;

/**
 * A node of a container's tree: an entry of the container's list, or a section grouping entries.
 *
 * @param <E> the type of the entries
 */
public sealed interface ContainerNode<E> permits ContainerEntry, ContainerSection {}
