package com.sitepark.ies.aggregator;

import java.util.List;

/**
 * Optional capability for a {@link RootAggregatorFactory} that builds a page from the steps of its
 * object type - the Java counterpart of {@code getResource.spml}'s loop over {@code steps}.
 *
 * <p>The steps are the editing masks of the object type ({@code objectTypes.<type>.steps}), each
 * with its section types. The platform reads them for the object of the generation, decides what is
 * placed in the current channel and creates the aggregators, as it does for the children of a
 * container ({@link SubAggregatorAware}, {@link SubSectionTypesAware}); the factory only works the
 * list off. It is handed in before {@link RootAggregatorFactory#create(int)} for the same object.
 *
 * <p>What the list holds:
 *
 * <ul>
 *   <li>the steps in their order ({@code order}, ties in the order of the configuration), without a
 *       step that is switched off ({@code enabled: false}) or not enabled for the nature of the
 *       channel ({@code aggregator.natures});
 *   <li>per step its section types in the same order and under the same rule, without a section type
 *       that has no aggregator at all (no {@code aggregator.uri} and no class - an editing-only
 *       type, which {@code getResource.spml} passes over);
 *   <li>a section type that has an SPML aggregator but no Java class with its configuration and
 *       without an aggregator, so that the factory sees it and can refuse the page.
 * </ul>
 */
public interface StepsAware {

  /**
   * Sets the steps of the object the next {@link RootAggregatorFactory#create(int)} is for.
   *
   * @param steps the placed steps, in order
   */
  void setSteps(List<Step> steps);
}
