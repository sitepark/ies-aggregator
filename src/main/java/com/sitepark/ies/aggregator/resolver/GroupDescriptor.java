package com.sitepark.ies.aggregator.resolver;

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
   * #isMicrositeRootSite()} are {@code false} and {@link #lang()} is empty.
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
}
