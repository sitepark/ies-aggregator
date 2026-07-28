package com.sitepark.ies.aggregator.value.media.scaling;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PaddingTest {

  @Test
  void externalNameIsTheCmsFacingSpelling() {
    assertThat(Padding.NO.getName()).as("NO should expose the external name 'no'").isEqualTo("no");
    assertThat(Padding.YES.getName())
        .as("YES should expose the external name 'yes'")
        .isEqualTo("yes");
    assertThat(Padding.FIT.getName())
        .as("FIT should expose the external name 'fit'")
        .isEqualTo("fit");
  }

  @Test
  void toStringReturnsTheExternalName() {
    assertThat(Padding.NO.toString())
        .as("toString() should return the external name rather than the constant name")
        .isEqualTo("no");
  }
}
