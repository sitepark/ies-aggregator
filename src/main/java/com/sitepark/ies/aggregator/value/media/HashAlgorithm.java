package com.sitepark.ies.aggregator.value.media;

import com.sitepark.ies.aggregator.value.NamedEnum;

/** Hash algorithm used to fingerprint a media asset's content. */
public enum HashAlgorithm implements NamedEnum {
  MD5("md5"),
  SHA_256("sha-256");

  private final String name;

  HashAlgorithm(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return this.name;
  }
}
