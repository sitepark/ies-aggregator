package com.sitepark.ies.aggregator.value.uri;

/**
 * Describes what a {@link com.sitepark.ies.aggregator.port.ChannelUriProvider ChannelUriProvider}
 * should resolve a URI for.
 *
 * <p>The port resolves URIs by <em>data</em>, not by API surface: new resolution cases are added as
 * new {@code UriTarget} variants rather than as new port methods.
 */
public sealed interface UriTarget {

  /**
   * Creates a target for a standalone CMS object.
   *
   * @param objectId the id of the object to resolve the URI for
   * @return the URI target
   */
  static UriTarget ofObject(int objectId) {
    return new ObjectTarget(objectId);
  }

  /**
   * Creates a target for a media binary related to that object.
   *
   * @param objectId the id of the object the media was uploaded to
   * @param mediaId the id of the uploaded media binary within that object
   * @return the URI target
   */
  static UriTarget ofMedia(int objectId, int mediaId) {
    return new MediaTarget(objectId, mediaId);
  }

  /**
   * A standalone CMS object (page/article), addressed by its object id.
   *
   * @param objectId the id of the object to resolve the URI for
   */
  record ObjectTarget(int objectId) implements UriTarget {}

  /**
   * A media binary related to that object.
   *
   * @param objectId the id of the object the media was uploaded to
   * @param mediaId the id of the uploaded media binary within that object
   */
  record MediaTarget(int objectId, int mediaId) implements UriTarget {}
}
