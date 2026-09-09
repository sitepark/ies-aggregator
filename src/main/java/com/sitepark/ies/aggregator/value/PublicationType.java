package com.sitepark.ies.aggregator.value;

/**
 * What kind of artefact a channel publishes.
 *
 * <p>Not what a resource <em>is</em>, but what role the published file plays: the same aggregation
 * can end up as the page of an object, as the meta data of a medium, or as a configuration file the
 * runtime reads. The role decides where the file goes and, together with {@link ResourcePathType},
 * what shape it has.
 *
 * <p>The external name (see {@link #getName()}) is the stable spelling used in configuration;
 * {@link #toString()} returns the same name.
 */
public enum PublicationType implements NamedEnum {

  /** The page of a CMS object. The default when a channel configures nothing. */
  OBJECT("object"),

  /** The meta data of a medium published on its own. */
  MEDIA("media"),

  /** The meta data of a medium uploaded into a field of an object. */
  EMBEDDED_MEDIA("embeddedMedia"),

  /** Redirect rules for the web server. */
  REDIRECT_RULES("redirectRules"),

  /** Access rules for the web server. */
  SECURITY_RULES("securityRules"),

  /** Account data for the web server. */
  SECURITY_USERS("securityUsers"),

  /** Configuration read by the consuming runtime. */
  CONFIG("config");

  private final String name;

  PublicationType(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
