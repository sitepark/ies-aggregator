package com.sitepark.ies.aggregator.container;

import org.jspecify.annotations.Nullable;

/**
 * An entry of a container's list, lying in a section or directly in the container.
 *
 * @param entry the entry
 * @param index the position of the entry in the container's list; fixed section types are chosen
 *     by it
 * @param rule how the entry takes part in the hierarchy, or {@code null} if its section type is not
 *     configured for the container; the former aggregator leaves such entries out, the tree keeps
 *     them in place
 * @param <E> the type of the entries
 */
public record ContainerEntry<E>(E entry, int index, @Nullable HierarchyRule rule)
    implements ContainerNode<E> {}
