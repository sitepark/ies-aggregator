package com.sitepark.ies.aggregator.value;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * A non-translatable {@link Uri}: an immutable wrapper for a {@link java.net.URI} that exposes
 * individual URI components and is rendered verbatim.
 *
 * <p>Use the static factory methods {@link #of(URI)} and {@link #of(String)} to construct instances.
 * The constructor is package-private to restrict construction to the static factory methods. Can be
 * promoted to a {@link TranslatableUri} via {@link #translatable()}.
 */
public final class PlainUri implements Uri {

  private final URI originUri;

  PlainUri(URI originUri) {
    this.originUri = originUri;
  }

  /**
   * Creates a {@code PlainUri} wrapping the given {@link URL} using {@link URL#toURI()}.
   *
   * @param url the URL to wrap
   */
  public static PlainUri of(URL url) {
    try {
      return new PlainUri(url.toURI());
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }

  /**
   * Creates a {@code PlainUri} wrapping the given {@link URI}.
   *
   * @param uri the URI to wrap
   */
  public static PlainUri of(URI uri) {
    return new PlainUri(uri);
  }

  /**
   * Creates a {@code PlainUri} from the given URI string.
   *
   * @param uri the URI string
   * @throws IllegalArgumentException if the string violates RFC 2396
   */
  public static PlainUri of(String uri) {
    return new PlainUri(URI.create(uri));
  }

  /**
   * Returns a copy of {@code uri} with the path component replaced.
   *
   * @param uri the base URI
   * @param path the new path
   * @return a new {@code PlainUri} with the updated path
   * @throws IllegalArgumentException if the resulting URI is invalid
   */
  public static PlainUri updatePath(PlainUri uri, String path) {
    try {
      URI updatedUri =
          new URI(
              uri.scheme(),
              uri.userInfo(),
              uri.host(),
              uri.port(),
              path,
              uri.query(),
              uri.fragment());
      return new PlainUri(updatedUri);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Invalid URI path update", e);
    }
  }

  /** Returns true if this URI is absolute, false otherwise. */
  public boolean isAbsolute() {
    return this.originUri.isAbsolute();
  }

  /** Returns the scheme component of this URI. */
  public String scheme() {
    return this.originUri.getScheme();
  }

  /** Returns the user-info component of this URI, or {@code null}. */
  public String userInfo() {
    return this.originUri.getUserInfo();
  }

  /** Returns the host component of this URI, or {@code null}. */
  public String host() {
    return this.originUri.getHost();
  }

  /** Returns the port number, or {@code -1} if undefined. */
  public int port() {
    return this.originUri.getPort();
  }

  /** Returns the authority of this URI, or {@code null}. */
  public String authority() {
    return this.originUri.getAuthority();
  }

  /** Returns the path component of this URI. */
  public String path() {
    return this.originUri.getPath();
  }

  /** Returns the query component, or {@code null}. */
  public String query() {
    return this.originUri.getQuery();
  }

  /** Returns the fragment component, or {@code null}. */
  public String fragment() {
    return this.originUri.getFragment();
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof PlainUri that) && this.originUri.equals(that.originUri);
  }

  @Override
  public int hashCode() {
    return this.originUri.hashCode();
  }

  /**
   * Returns this URI reduced to an <em>absolute-path reference</em> as defined by <a
   * href="https://datatracker.ietf.org/doc/html/rfc3986#section-4.2">RFC 3986, Section 4.2</a>.
   *
   * <p>The resulting URI contains only the path (starting with {@code /}), along with the query and
   * fragment components if present. Scheme and authority are stripped. For example:
   *
   * <pre>{@code
   * https://example.com/foo/bar?x=1#section  ->  /foo/bar?x=1#section
   * }</pre>
   *
   * @return a new {@code PlainUri} consisting of path, query and fragment only
   * @throws IllegalArgumentException if the path does not start with {@code /} (i.e. is not an
   *     absolute path), or if the resulting URI cannot be constructed
   */
  public PlainUri toAbsolutePathReference() {
    if (!this.path().startsWith("/")) {
      throw new IllegalArgumentException("Path must be absolute");
    }
    try {
      URI updatedUri = new URI(null, null, null, 0, this.path(), this.query(), this.fragment());
      return new PlainUri(updatedUri);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Invalid URI path update", e);
    }
  }

  /**
   * Returns an <em>absolute URI</em> as defined by <a
   * href="https://datatracker.ietf.org/doc/html/rfc3986#section-4.3">RFC 3986, Section 4.3</a> by
   * combining this absolute-path reference with the scheme and authority of the given base URI.
   *
   * <p>The path, query and fragment of this URI are preserved; scheme and authority are taken from
   * {@code base}. For example:
   *
   * <pre>{@code
   * this: /foo/bar?x=1#section
   * base: https://example.com/other/path
   * ->    https://example.com/foo/bar?x=1#section
   * }</pre>
   *
   * @param base the URI providing scheme and authority; must be absolute and have an authority
   *     component
   * @return a new absolute {@code PlainUri}
   * @throws IllegalArgumentException if this URI is not an absolute-path reference, if {@code base}
   *     is not absolute or has no authority, or if the resulting URI cannot be constructed
   */
  public PlainUri toAbsoluteUri(PlainUri base) {
    if (!this.path().startsWith("/")) {
      throw new IllegalArgumentException("This URI must be an absolute-path reference");
    }
    if (!base.isAbsolute()) {
      throw new IllegalArgumentException("Base URI must be absolute");
    }
    if (base.authority() == null) {
      throw new IllegalArgumentException("Base URI must have an authority");
    }
    try {
      URI updatedUri =
          new URI(base.scheme(), base.authority(), this.path(), this.query(), this.fragment());
      return new PlainUri(updatedUri);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Invalid URI construction", e);
    }
  }

  public TranslatableUri translatable() {
    return TranslatableUri.of(this);
  }

  @Override
  public String toString() {
    return this.originUri.toString();
  }
}
