package com.sitepark.ies.aggregator.value.uri;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

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

  @Test
  void testEmptyUri() {
    assertThat(Uri.of("").isEmpty()).as("Uri.of() should produce an empty Uri").isTrue();
  }

  @Test
  void orElseWithValueReturnsFallbackWhenEmpty() {
    Uri fallback = Uri.of("https://example.com/fallback");

    assertThat(PlainUri.EMPTY.orElse(fallback))
        .as("An empty URI should be replaced by the eager fallback")
        .isSameAs(fallback);
  }

  @Test
  void orElseWithValueReturnsThisWhenNonEmpty() {
    Uri uri = Uri.of("https://example.com/value");

    assertThat(uri.orElse(Uri.of("https://example.com/fallback")))
        .as("A non-empty URI should keep its own value, ignoring the eager fallback")
        .isSameAs(uri);
  }

  @Test
  void orElseWithSupplierReturnsFallbackWhenEmpty() {
    Uri fallback = Uri.of("https://example.com/fallback");

    assertThat(PlainUri.EMPTY.orElse(() -> fallback))
        .as("An empty URI should be replaced by the supplied fallback")
        .isSameAs(fallback);
  }

  @Test
  void orElseWithSupplierReturnsThisWithoutEvaluatingFallbackWhenNonEmpty() {
    Uri uri = Uri.of("https://example.com/value");

    Uri result =
        uri.orElse(() -> fail("Fallback supplier must not be evaluated for a non-empty URI"));

    assertThat(result).as("A non-empty URI should keep its own value").isSameAs(uri);
  }
}
