package com.sitepark.ies.aggregator.value;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class UrlMappingModeTest {

  @ParameterizedTest
  @EnumSource(UrlMappingMode.class)
  void externalNameIsTheConstantName(UrlMappingMode mode) {
    assertThat(mode.getName())
        .as("the external name should be the constant name")
        .isEqualTo(mode.name());
    assertThat(mode).as("toString() should return the external name").hasToString(mode.getName());
  }
}
