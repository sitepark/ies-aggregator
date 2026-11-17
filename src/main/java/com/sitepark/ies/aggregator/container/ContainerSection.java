package com.sitepark.ies.aggregator.container;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * A section of a container's tree, as the former aggregator ({@code SectionHierarchyManager} of
 * SiteKit) creates it.
 *
 * @param outputType the type the former aggregator gives the section in its output
 * @param implicit whether the former aggregator marks the section as implicit ({@code
 *     model.implicit}): sections before the first opened section and sections filling a gap of
 *     levels, not sections opened by an entry
 * @param ordinal the position of the section in the order in which the former aggregator creates
 *     the sections of the container, starting at 1; it draws the id of each section in this order,
 *     also for sections it drops later because they stay empty
 * @param opener the entry that opened the section and whose data are the data of the section, or
 *     {@code null} if the section only groups entries
 * @param children the entries and sections in the section
 * @param <E> the type of the entries
 */
public record ContainerSection<E>(
    String outputType,
    boolean implicit,
    int ordinal,
    @Nullable ContainerEntry<E> opener,
    List<ContainerNode<E>> children)
    implements ContainerNode<E> {

  public ContainerSection {
    children = List.copyOf(children);
  }
}
