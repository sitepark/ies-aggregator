package com.sitepark.ies.aggregator;

import java.util.Map;

/**
 * Optional capability for {@link Aggregator}s that place the output of their sub-aggregators
 * themselves - a section container.
 *
 * <p>A {@link SubAggregatorAware} aggregator gets the sub-aggregators only. A container also has to
 * know how each of its section types is configured before it lets that type's aggregator run:
 * whether the type is switched off, the type and id of the component it gets, the scope it reads.
 * The former aggregator read that from the section type's configuration for every entry ({@code
 * sectionType.spml}); the orchestrator hands the same part of the configuration in here, after
 * {@link SubAggregatorAware#setSubAggregators} and before the call to {@link Aggregator#aggregate}.
 *
 * <p>The keys are those of the sub-aggregators: the key of the section type in {@code sectionTypes}
 * for a selectable section type, {@value #FIXED_KEY_PREFIX} followed by the position (e.g. {@code
 * #fixed:0}) for a fixed section type of {@code fixedSectionTypes}. Unlike the sub-aggregators, the
 * map holds every section type the container offers, also one that has no Java aggregator: the
 * container sees its configuration even where it cannot run it.
 */
public interface SubSectionTypesAware {

  /**
   * Prefix of the key of a fixed section type, in this map and in the map of sub-aggregators; the
   * position follows. A {@code #} cannot start the key of a section type, so the two never collide.
   */
  String FIXED_KEY_PREFIX = "#fixed:";

  /**
   * Set the configuration of the section types this aggregator may place.
   *
   * @param subSectionTypes the section types, keyed as the sub-aggregators are
   */
  void setSubSectionTypes(Map<String, SubSectionType> subSectionTypes);
}
