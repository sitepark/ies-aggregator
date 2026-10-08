package com.sitepark.ies.aggregator.port;

import com.sitepark.ies.aggregator.value.AccessRestriction;
import com.sitepark.ies.aggregator.value.ResourcePathType;
import com.sitepark.ies.aggregator.value.uri.PlainUri;
import com.sitepark.ies.aggregator.value.uri.UriTarget;
import java.util.Optional;

/**
 * A publication channel through which CMS objects are accessible via URL.
 *
 * <p>Instances are channel-bound: every method answers for <em>this</em> channel. An injected {@code
 * Channel} is always the currently active one, which covers the common case without a lookup. Other
 * channels are obtained from {@link ChannelProvider}.
 */
public interface Channel {

  /**
   * The id of this channel.
   *
   * @return the channel id
   */
  int id();

  /**
   * The name of this channel.
   *
   * @return the channel name
   */
  String name();

  /**
   * The character encoding the content of this channel is published in.
   *
   * <p>A property of the channel, not of a single resource: a consumer that reads generated output
   * needs it to decode the bytes, and two channels of the same installation may differ.
   *
   * @return the encoding name, e.g. {@code UTF-8}
   */
  String encoding();

  /**
   * What kind of site this channel publishes — {@code internet}, {@code intranet}, {@code citycall},
   * {@code xzufi} and the like.
   *
   * <p>Distinct from {@link #name()}: the name identifies one channel, the nature says which of a
   * handful of kinds it is, and rules that differ between the public web and an internal one are
   * written against it. It is configuration of the channel itself, so a channel that never declared
   * one answers empty rather than a guessed default.
   *
   * @return the nature of this channel, or empty if the channel declares none
   */
  Optional<String> nature();

  /**
   * The value of a configured attribute of this channel.
   *
   * <p>Attributes are the extension point of the channel configuration: a product or a customer
   * puts its own switches there, and nothing in the platform knows what they mean. That is why the
   * value is answered as text — the caller owns the interpretation.
   *
   * <p>An empty answer means the attribute is <em>not set</em>, which is not the same as being set
   * to {@code false}. A rule that treats the two alike has to say so itself; this method will not
   * decide it.
   *
   * @param name the name of the attribute
   * @return the attribute value, or empty if this channel does not set it
   */
  Optional<String> attribute(String name);

  /**
   * How this channel addresses the resources it publishes.
   *
   * <p>The scheme changes the document, not only the file name: a resource addressed by its path may
   * have to resolve its own context when loaded, while one addressed by its id can be plain data.
   * Which of the two a writer produces is therefore a property of the channel.
   *
   * @return the addressing scheme; never {@code null}, {@link ResourcePathType#URL} unless the
   *     channel configures otherwise
   */
  ResourcePathType resourcePathType();

  /**
   * How this channel finds an object it does not publish itself, as its nature configures it.
   *
   * @return the lookup, or empty if the channel's nature configures none - then such an object is
   *     not linked at all
   */
  Optional<UrlLookup> urlLookup();

  /**
   * Whether the object with the given id is published in this channel.
   *
   * @param objectId the id of the object to check
   * @return {@code true} if the object is published in this channel
   */
  boolean isPublished(int objectId);

  /**
   * The access restriction the given object is under in this channel.
   *
   * <p>Publication is not the same as visibility: an object can be published and still be readable
   * only by certain groups. A consumer that generates output for a channel needs both.
   *
   * @param objectId the id of the object to check
   * @return the restriction, or empty if the object is unrestricted — the normal case
   */
  Optional<AccessRestriction> accessRestriction(int objectId);

  /**
   * Resolves the URI under which the given target is accessible in this channel.
   *
   * <p>For the current channel the URI is a path, since the resource being aggregated is published
   * on the same site. For any other channel it is the full URL including the host: a path would
   * point into the current site, where the target is not published.
   *
   * @param target what to resolve the URI for (e.g. a standalone object or an article media binary)
   * @return the resolved URI, or empty if no URI can be determined
   */
  Optional<PlainUri> resolveUri(UriTarget target);
}
