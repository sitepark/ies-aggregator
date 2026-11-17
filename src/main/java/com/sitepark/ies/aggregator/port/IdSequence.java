package com.sitepark.ies.aggregator.port;

/**
 * The ids of the components of the resource being generated.
 *
 * <p>SiteKit gives every component of a page an id of the form {@code <name>-<n>}: {@code text-1},
 * {@code text-2}, {@code section-1}. One counter per name runs across the whole resource, so ids
 * stay unique however the components are nested - a section, the container it lies in and the
 * sections the container opens all draw from the same counters. While SPML and Java build a page
 * together, both have to draw from the same counters, or an id is given twice.
 *
 * <p>An implementation hands out the counters of the resource being generated, shared with the
 * template where one builds the same resource.
 */
@FunctionalInterface
public interface IdSequence {

  /**
   * Draws the next id for a name.
   *
   * @param name the name, usually a section type or a component type - e.g. {@code text}
   * @return the id, {@code <name>-<n>}, where {@code n} counts from 1 per name and resource
   */
  String next(String name);
}
