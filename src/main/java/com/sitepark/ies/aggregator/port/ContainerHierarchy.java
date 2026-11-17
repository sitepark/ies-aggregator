package com.sitepark.ies.aggregator.port;

import com.sitepark.ies.aggregator.container.ContainerEntry;
import com.sitepark.ies.aggregator.container.ContainerNode;
import com.sitepark.ies.aggregator.container.ContainerSection;
import com.sitepark.ies.aggregator.resolver.Resolver;
import java.util.List;

/**
 * The content of a section container as a tree of sections.
 *
 * <p>This is the port for what SiteKit's {@code contentContainer.spml} does with its {@code
 * SectionHierarchyManager}: it reads the entries of a container's list and groups them into
 * sections - an entry such as {@code newSection} opens one, entries before the first one go into an
 * implicit section, levels nest, a gap of levels is filled. Which entry does what comes from the
 * configuration of the object type; only the platform knows it, and on one platform the tree may be
 * stored rather than derived.
 *
 * <p>The answer is purely structural. It carries no ids and drops nothing: a section opened by an
 * entry stays even without children, and an entry whose section type is not configured stays in
 * place with no {@link ContainerEntry#rule()}. Whoever renders the tree draws the ids - in the order
 * of {@link ContainerSection#ordinal()}, also for sections it drops - leaves out sections that render
 * nothing and skips unconfigured entries, as the former aggregator does.
 */
@FunctionalInterface
public interface ContainerHierarchy {

  /**
   * Returns the tree of a container.
   *
   * @param scope the resolver that holds the container's list, e.g. the article or, for a container
   *     nested in a section, that section's data
   * @param iteratorFieldName the name of the list field, as configured in {@code
   *     template.iteratorFieldName} - e.g. {@code sp_main_iterate}
   * @return the top-level nodes of the container, each entry with its data as {@link Resolver};
   *     empty if the list is empty or no container with that list is configured
   */
  List<ContainerNode<Resolver>> of(Resolver scope, String iteratorFieldName);
}
