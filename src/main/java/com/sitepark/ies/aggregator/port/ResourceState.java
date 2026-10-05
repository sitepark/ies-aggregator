package com.sitepark.ies.aggregator.port;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * State that belongs to the resource being aggregated rather than to a single aggregator call.
 *
 * <p>A resource is not always built by one aggregation. While templates still drive the
 * publication, every {@code sp:aggregator} call aggregates a single section on a root of its own,
 * and the template merges the result into the resource it builds itself. What one section leaves
 * for the next - a page-wide count, say - then has to live outside the call. This port provides
 * both halves of that:
 *
 * <ul>
 *   <li>{@link #attribute} - a store that lives as long as the resource is being published, shared
 *       by every aggregation that contributes to it,
 *   <li>{@link #enclosing} - a read-only view of the resource an enclosing template has built so
 *       far, including what earlier template sections wrote into it.
 * </ul>
 */
public interface ResourceState {

  /**
   * Returns the object the current resource holds under the given key, creating it on first access.
   *
   * <p>Every aggregation that contributes to the same resource receives the same instance; the next
   * resource starts without it.
   *
   * @param <T> the type of the stored object
   * @param key the key the object is stored under, by convention its own class
   * @param factory creates the object on first access within a resource
   * @return the object held for the current resource
   */
  <T> T attribute(Class<T> key, Supplier<T> factory);

  /**
   * The resource an enclosing template has built up to this aggregation, area by area.
   *
   * <p>Present only when the aggregation runs inside a template that builds the resource itself -
   * the {@code sp:aggregator} case. When the aggregator builds the resource on its own, the tree it
   * writes into <em>is</em> the resource and there is nothing enclosing it: read that tree instead.
   *
   * <p>A snapshot of what the template holds when the aggregation starts. It reflects nothing the
   * running aggregation writes, and it must not be modified: writing goes through the output node
   * alone. The values are what the template stored - maps, lists, strings, numbers and booleans -
   * so a number may arrive as any {@link Number} or as its text.
   *
   * @return the areas of the enclosing resource, or empty if there is no enclosing template
   */
  Optional<Map<String, Object>> enclosing();
}
