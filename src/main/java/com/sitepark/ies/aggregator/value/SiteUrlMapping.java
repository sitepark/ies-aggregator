package com.sitepark.ies.aggregator.value;

import java.util.Locale;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * One URL mapping of a site: a path below the site that is served or redirected to an object.
 *
 * <p>A mapping is what an editor configured beside the object's own address, such as a short URL or
 * the old address of a moved page. It belongs to a site and to the channel the site is published
 * in; see {@link com.sitepark.ies.aggregator.resolver.GroupDescriptor#urlMappings()}.
 *
 * @param path the path the mapping answers, as configured; never empty
 * @param ownerId the id of the object the path leads to, in the spelling the platform stores it; never
 *     empty
 * @param mode how the path is answered
 * @param locale the language the mapping applies to, or {@code null} if it applies to every
 *     language
 */
public record SiteUrlMapping(
    String path, String ownerId, UrlMappingMode mode, @Nullable Locale locale) {

  /**
   * Creates a mapping.
   *
   * @param path the path the mapping answers; must not be {@code null}
   * @param ownerId the id of the object the path leads to; must not be {@code null}
   * @param mode how the path is answered; must not be {@code null}
   * @param locale the language the mapping applies to, or {@code null} for every language
   */
  public SiteUrlMapping {
    Objects.requireNonNull(path, "path");
    Objects.requireNonNull(ownerId, "ownerId");
    Objects.requireNonNull(mode, "mode");
  }
}
