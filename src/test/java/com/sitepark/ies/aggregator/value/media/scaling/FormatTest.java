package com.sitepark.ies.aggregator.value.media.scaling;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FormatTest {

  @ParameterizedTest
  @CsvSource({
    "AVIF, avif, image/avif",
    "WEBP, webp, image/webp",
    "JPEG, jpeg, image/jpeg",
    "PNG, png, image/png",
    "GIF, gif, image/gif"
  })
  void exposesConfigTokenAndMimeType(Format format, String name, String mimeType) {
    assertThat(format.getName())
        .as("%s should expose the config token '%s'", format.name(), name)
        .isEqualTo(name);
    assertThat(format.mimeType())
        .as("%s should expose the media type '%s'", format.name(), mimeType)
        .isEqualTo(mimeType);
  }

  @Test
  void toStringReturnsTheExternalName() {
    assertThat(Format.AVIF.toString())
        .as("toString() should return the external name rather than the constant name")
        .isEqualTo("avif");
  }
}
