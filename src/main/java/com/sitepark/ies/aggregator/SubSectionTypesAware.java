package com.sitepark.ies.aggregator;

import java.util.Map;

/**
 * Optional capability for {@link Aggregator}s that place the output of their sub-aggregators
 * themselves - a section container.
 *
 * <p>A {@link SubAggregatorAware} aggregator gets the sub-aggregators only. A container also has to
 * know how each of its section types is configured before it lets that type's aggregator run:
 * the type and id of the component an entry gets, and the scope it reads.
 * The former aggregator read that from the section type's configuration for every entry ({@code
 * sectionType.spml}); the orchestrator hands the same part of the configuration in here, after
 * {@link SubAggregatorAware#setSubAggregators} and before the call to {@link Aggregator#aggregate}.
 *
 * <p>The keys are those of the sub-aggregators: the key of the section type in {@code sectionTypes}
 * for a selectable section type, {@value #FIXED_KEY_PREFIX} followed by the position (e.g. {@code
 * #fixed:0}) for a fixed section type of {@code fixedSectionTypes}.
 *
 * <p>The map holds the section types that are placed in the current channel. Unlike the
 * sub-aggregators it holds such a type also when it has no Java aggregator: the container sees its
 * configuration even where it cannot run it. A section type that is not placed - switched off by
 * {@code enabled: false}, or not enabled for the nature of the channel by {@code
 * aggregator.natures} - is in neither map; an entry of such a type is left out without a trace, as
 * {@code sectionType.spml} leaves it out before it draws the entry's id.
 *
 * <p>A {@link RootAggregatorFactory} that builds a page from its object type's steps gets the
 * same, keyed {@code <step>/<section type>} and in the order the section types are placed in (see
 * {@link SubAggregatorAware}). A step that is switched off or not enabled for the channel's nature
 * contributes none of its section types; a section type without any aggregator - an editing-only
 * type, configured without {@code aggregator.uri} and class - is not placed. A placed section type
 * that has only its SPML aggregator is in this map and not among the sub-aggregators, so the
 * factory can refuse the page.
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
