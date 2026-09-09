package com.sitepark.ies.aggregator.value;

/**
 * How a channel addresses the resources it publishes.
 *
 * <p>The two schemes lead to two different documents for the same resource. Under {@link #URL} a
 * resource is addressed by its path in the site, and the published file is loaded by that path —
 * which is why it may have to be an executable script that resolves its own context. Under {@link
 * #ID} it is addressed by its object id, and the file can be plain data, because whoever loads it
 * already knows what it is.
 *
 * <p>Which document a writer produces is therefore not a property of the resource but of the
 * channel it is published into (see {@link com.sitepark.ies.aggregator.port.Channel}).
 *
 * <p>The external name (see {@link #getName()}) is the stable spelling used in configuration;
 * {@link #toString()} returns the same name.
 */
public enum ResourcePathType implements NamedEnum {

  /** Addressed by its path in the site. The default when a channel configures nothing. */
  URL("url"),

  /** Addressed by its object id. */
  ID("id");

  private final String name;

  ResourcePathType(String name) {
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
