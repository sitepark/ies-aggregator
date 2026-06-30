package com.sitepark.ies.aggregator.value.media;

/** Content hash of a media asset: the {@link HashAlgorithm} together with the hex digest value. */
public record Hash(HashAlgorithm algorithm, String value) {}
