package com.sitepark.ies.aggregator;

import java.util.List;

/**
 * A step of an object type as it is placed in the current channel: one editing mask with the
 * section types it holds (see {@link StepsAware}).
 *
 * @param id the id the configuration names the step by; never empty
 * @param sectionTypes the step's placed section types, in order
 */
public record Step(String id, List<PlacedSectionType> sectionTypes) {

  /** Copies the list; it is immutable. */
  public Step {
    sectionTypes = List.copyOf(sectionTypes);
  }
}
