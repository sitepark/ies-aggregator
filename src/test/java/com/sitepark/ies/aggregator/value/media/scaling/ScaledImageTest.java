package com.sitepark.ies.aggregator.value.media.scaling;

import static org.assertj.core.api.Assertions.assertThat;

import com.sitepark.ies.aggregator.value.uri.Uri;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ScaledImageTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(ScaledImage.class)
        .withPrefabValues(Uri.class, Uri.of("https://a.example/"), Uri.of("https://b.example/"))
        .verify();
  }

  @Test
  void exposesUrlAndResultDimensions() {
    Uri url = Uri.of("https://example.com/scaled.jpg");
    ScaledImage image = ScaledImage.of(url, 800, 600);

    assertThat(image.url()).as("url() should return the constructor argument").isSameAs(url);
    assertThat(image.width()).as("width() should return the result width").isEqualTo(800);
    assertThat(image.height()).as("height() should return the result height").isEqualTo(600);
  }
}
