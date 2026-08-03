package com.sitepark.ies.aggregator.value.media.scaling;

import static org.assertj.core.api.Assertions.assertThat;

import com.jparams.verifier.tostring.ToStringVerifier;
import com.sitepark.ies.aggregator.value.uri.Uri;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class EncodedTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(Encoded.class)
        .withPrefabValues(Uri.class, Uri.of("https://a.example/"), Uri.of("https://b.example/"))
        .verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(Encoded.class).verify();
  }

  @Test
  void exposesFormatAndUrl() {
    Uri url = Uri.of("https://example.com/scaled.avif");
    Encoded encoded = Encoded.of(Format.AVIF, url);

    assertThat(encoded.format())
        .as("format() should return the produced format")
        .isEqualTo(Format.AVIF);
    assertThat(encoded.url()).as("url() should return the constructor argument").isSameAs(url);
  }
}
