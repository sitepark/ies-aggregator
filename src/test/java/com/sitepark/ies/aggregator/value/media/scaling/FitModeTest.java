package com.sitepark.ies.aggregator.value.media.scaling;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FitModeTest {

  @Test
  void externalNameIsTheConfigFacingSpelling() {
    assertThat(FitMode.COVER.getName())
        .as("COVER should expose the external name 'cover'")
        .isEqualTo("cover");
    assertThat(FitMode.CONTAIN.getName())
        .as("CONTAIN should expose the external name 'contain'")
        .isEqualTo("contain");
    assertThat(FitMode.PAD.getName())
        .as("PAD should expose the external name 'pad'")
        .isEqualTo("pad");
    assertThat(FitMode.COVER_OR_PAD.getName())
        .as("COVER_OR_PAD should expose the external name 'coverOrPad'")
        .isEqualTo("coverOrPad");
  }

  @Test
  void toStringReturnsTheExternalName() {
    assertThat(FitMode.COVER_OR_PAD.toString())
        .as("toString() should return the external name rather than the constant name")
        .isEqualTo("coverOrPad");
  }
}
