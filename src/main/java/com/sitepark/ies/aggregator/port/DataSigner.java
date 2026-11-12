package com.sitepark.ies.aggregator.port;

import java.util.Map;

/**
 * Signs data that the aggregated resource writes out and that a client later sends back to the
 * IES, so that the IES can trust it again (for example sender and roles of a web account).
 *
 * <p>The returned token is opaque: the resource writes it out next to the data in clear, and the
 * client passes it back unchanged. Tokens expire; signing also asks the publisher to regenerate the
 * resource before that (see {@link Regeneration}), so the published token stays valid.
 */
@FunctionalInterface
public interface DataSigner {

  /**
   * Signs the data for the purpose.
   *
   * @param purpose what the token is meant for, e.g. {@code web-account}; lower case letters,
   *     digits, dots and dashes
   * @param data the data to sign; values must be serializable as JSON
   * @return the token
   * @throws IllegalArgumentException if the purpose is invalid
   */
  String sign(String purpose, Map<String, ?> data);
}
