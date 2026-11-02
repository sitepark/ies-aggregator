package com.sitepark.ies.aggregator;

/**
 * Optional capability for {@link Aggregator}s of a section type that need to know which section
 * type they aggregate.
 *
 * <p>A section type can inherit another one's aggregator and still be a type of its own - a
 * project's variant of a search, for one. The aggregator is then the same class for both, and a
 * project that extends only the variant has to tell them apart. The orchestrator hands in the id
 * the configuration names the section type by, before the call to {@link Aggregator#aggregate}.
 */
public interface SectionTypeAware {

  /**
   * Set the id of the section type being aggregated.
   *
   * @param sectionTypeId the id the configuration names the section type by, e.g. {@code
   *     listSearch}; never empty
   */
  void setSectionType(String sectionTypeId);
}
