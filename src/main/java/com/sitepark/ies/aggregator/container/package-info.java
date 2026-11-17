/**
 * The content of a section container as a tree: the entries of its list and the sections they are
 * grouped into, as the former aggregator ({@code SectionHierarchyManager} of SiteKit) groups them.
 *
 * <p>"Container" because two meanings of "section" meet here: in the CMS every entry of a container
 * list is a section, while in the output a section is the grouping that an entry such as {@code
 * newSection} opens. The entries are {@link com.sitepark.ies.aggregator.container.ContainerEntry},
 * the groupings {@link com.sitepark.ies.aggregator.container.ContainerSection}.
 *
 * <p>The types are generic over the entry, so that the host can arrange its own representation of
 * an entry with the same code; the port {@link com.sitepark.ies.aggregator.port.ContainerHierarchy}
 * answers with {@link com.sitepark.ies.aggregator.resolver.Resolver}s.
 */
@NullMarked
package com.sitepark.ies.aggregator.container;

import org.jspecify.annotations.NullMarked;
