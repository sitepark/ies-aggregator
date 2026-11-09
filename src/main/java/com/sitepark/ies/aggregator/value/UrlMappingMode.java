package com.sitepark.ies.aggregator.value;

/**
 * How a {@link SiteUrlMapping} answers a request for its path.
 *
 * <p>The external name (see {@link #getName()}) is the constant's own name, the spelling the runtime
 * reads from the mappings a site publishes; {@link #toString()} returns the same name.
 */
public enum UrlMappingMode implements NamedEnum {

  /** The path is an alias: the content of the owner is served directly (HTTP 200). */
  FORWARD,

  /** The URL has moved for good; search engines carry the ranking over (HTTP 301). */
  REDIRECT_PERMANENT,

  /** The target may change again; nothing is carried over (HTTP 302). */
  REDIRECT_TEMPORARY,

  /** A temporary redirect that keeps the HTTP method and body (HTTP 307). */
  REDIRECT_TEMPORARY_PRESERVE_METHOD,

  /** A permanent redirect that keeps the HTTP method and body (HTTP 308). */
  REDIRECT_PERMANENT_PRESERVE_METHOD;

  @Override
  public String getName() {
    return this.name();
  }

  @Override
  public String toString() {
    return this.name();
  }
}
