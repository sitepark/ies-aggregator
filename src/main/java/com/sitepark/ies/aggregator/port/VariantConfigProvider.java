package com.sitepark.ies.aggregator.port;

import com.sitepark.ies.aggregator.value.media.scaling.VariantConfig;
import java.util.Collection;
import java.util.Map;

/**
 * Looks up the scaling configuration of named image variants.
 *
 * <p>An image variant is a named rendition an editor selects rather than describes — {@code teaser},
 * {@code kachel} — and what it means in pixels, formats and aspect ratio is maintained outside the
 * aggregation, in the project configuration. This is therefore a port: the adapter that reads that
 * configuration is supplied by the platform, the aggregator only asks for it by name.
 *
 * <p>Variants are looked up in one call because a caller regularly needs several at once: a teaser
 * offers all its renditions together, while a content image asks for the single variant the editor
 * selected.
 */
@FunctionalInterface
public interface VariantConfigProvider {

  /**
   * Looks up the given variants in one call.
   *
   * @param variantNames the variant names to look up
   * @return the configuration per variant name; unknown names are absent from the result
   */
  Map<String, VariantConfig> get(Collection<String> variantNames);
}
