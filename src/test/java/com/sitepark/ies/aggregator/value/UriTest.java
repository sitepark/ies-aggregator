package com.sitepark.ies.aggregator.value;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import org.junit.jupiter.api.Test;

class UriTest {

  @Test
  void ofProducesNonTranslatablePlainUri() {
    Uri uri = Uri.of("https://example.com/foo");

    assertThat(uri)
        .as("Uri.of() should produce a non-translatable PlainUri")
        .isInstanceOf(PlainUri.class);
  }

  @Test
  void translatableUriIsAlsoAUri() {
    assertThat(TranslatableUri.of(URI.create("https://example.com/foo")))
        .as("TranslatableUri should be usable wherever a Uri is expected")
        .isInstanceOf(Uri.class);
  }
}
