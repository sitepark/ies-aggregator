package com.sitepark.ies.aggregator.resolver;

import com.sitepark.ies.aggregator.value.SiteUrlMapping;
import java.util.List;

/**
 * The master data of the group a {@link GroupResolver} reads from.
 *
 * <p>Extends {@link EntityDescriptor} with the fields that only a group has, the same way {@link
 * com.sitepark.ies.aggregator.value.media.ImageMetadata ImageMetadata} extends the shared media
 * metadata. {@link GroupResolver#entity()} narrows its return type to this interface, so callers of
 * a group resolver reach both the shared and the group-specific fields through one object.
 */
public interface GroupDescriptor extends EntityDescriptor {

  /**
   * Returns the group descriptor that carries no data.
   *
   * <p>Returned by {@link GroupResolver#entity()} of an empty group resolver: the inherited fields
   * behave like {@link EntityDescriptor#empty()}, {@link #isRootSite()} and {@link
   * #isMicrositeRootSite()} are {@code false} and {@link #lang()} is empty and {@link #urlMappings()} has no
   * mapping.
   *
   * @return the empty group descriptor
   */
  static GroupDescriptor empty() {
    return EmptyGroupDescriptor.INSTANCE;
  }

  /**
   * Whether this group is the root of a site.
   *
   * @return {@code true} if the group is a site root
   */
  boolean isRootSite();

  /**
   * Whether this group is the root of a microsite.
   *
   * @return {@code true} if the group is a microsite root
   */
  boolean isMicrositeRootSite();

  /**
   * The language of the content below this group, as an ISO language code.
   *
   * @return the language code, or the empty string if the group has none
   */
  String lang();

  /**
   * The URL mappings of the site this group is the root of, as configured for the channel being
   * generated.
   *
   * <p>A mapping answers a path below the site with an object, as an alias or a redirect. Mappings
   * are kept per site and per channel, so the same group answers differently in two channels, and a
   * group that is not the root of a site answers none. The list is ordered as the platform ranks the
   * mappings, and holds at most one mapping per path - the one with the highest priority.
   *
   * <p>Outside a generation there is no channel; what an implementation then answers is its own to
   * document.
   *
   * @return the mappings; never {@code null}, empty if the group is not a site root or the site has
   *     none
   */
  List<SiteUrlMapping> urlMappings();
}
