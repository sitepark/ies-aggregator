package com.sitepark.ies.aggregator.value.uri;

import com.sitepark.ies.aggregator.value.Emptiable;
import java.net.URI;
import java.net.URL;
import java.util.function.Supplier;

/**
 * A URI value an aggregator writes into the output tree.
 *
 * <p>A {@code Uri} is one of two kinds:
 *
 * <ul>
 *   <li>{@link PlainUri} — rendered verbatim and never translated; exposes the individual URI
 *       components.
 *   <li>{@link TranslatableUri} — whose path segments are collected into the translation table and
 *       rendered through it.
 * </ul>
 *
 * <p>Because the {@link com.sitepark.ies.aggregator.output.OutputVisitor OutputVisitor} dispatches
 * on the runtime type, a field declared as {@code Uri} lets the aggregator decide per value whether
 * a URI should be translatable — both kinds are written into the same field.
 */
public sealed interface Uri extends Emptiable permits PlainUri, TranslatableUri {

  static PlainUri empty() {
    return PlainUri.EMPTY;
  }

  PlainUri toPlainUri();

  Uri translatable();

  /**
   * Returns this URI when it is non-empty, otherwise {@code other}.
   *
   * @param other the replacement URI used when this URI is empty; must not be {@code null}
   * @return this URI when non-empty, otherwise {@code other}
   */
  default Uri orElse(Uri other) {
    return this.isEmpty() ? other : this;
  }

  /**
   * Returns this URI when it is non-empty, otherwise the URI produced by {@code fallback}.
   *
   * <p>The fallback is only evaluated when this URI is empty, so an expensive fallback (e.g.
   * assembling a URI from a linked object) is skipped whenever a value is already present.
   *
   * @param fallback supplies the replacement URI when this URI is empty; must not be {@code null}
   * @return this URI when non-empty, otherwise {@code fallback.get()}
   */
  default Uri orElse(Supplier<? extends Uri> fallback) {
    return this.isEmpty() ? fallback.get() : this;
  }

  /**
   * Creates a non-translatable, verbatim {@link PlainUri} wrapping the given {@link URL}.
   *
   * @param url the URL to wrap
   * @return a new {@link PlainUri} instance
   */
  static PlainUri of(URL url) {
    return PlainUri.of(url);
  }

  /**
   * Creates a non-translatable, verbatim {@link PlainUri} wrapping the given {@link URI}.
   *
   * @param uri the URI to wrap
   * @return a new {@link PlainUri} instance
   */
  static PlainUri of(URI uri) {
    return PlainUri.of(uri);
  }

  /**
   * Creates a non-translatable, verbatim {@link PlainUri} from the given URI string.
   *
   * @param uri the URI string
   * @return a new {@link PlainUri} instance
   * @throws IllegalArgumentException if the string violates RFC 2396
   */
  static PlainUri of(String uri) {
    return PlainUri.of(uri);
  }
}
