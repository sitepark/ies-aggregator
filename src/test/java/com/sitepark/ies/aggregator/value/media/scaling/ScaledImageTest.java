package com.sitepark.ies.aggregator.value.media.scaling;

import static org.assertj.core.api.Assertions.assertThat;

import com.sitepark.ies.aggregator.value.uri.Uri;
import java.util.ArrayList;
import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ScaledImageTest {

  private static Encoded anEncoding() {
    return Encoded.of(Format.JPEG, Uri.of("https://example.com/scaled.jpg"));
  }

  @Test
  void testEquals() {
    EqualsVerifier.forClass(ScaledImage.class)
        .withPrefabValues(Uri.class, Uri.of("https://a.example/"), Uri.of("https://b.example/"))
        .verify();
  }

  @Test
  void exposesResultDimensionsAndEncodings() {
    Encoded jpeg = anEncoding();
    Encoded avif = Encoded.of(Format.AVIF, Uri.of("https://example.com/scaled.avif"));

    ScaledImage image = ScaledImage.of(800, 600, List.of(jpeg, avif));

    assertThat(image.width()).as("width() should return the result width").isEqualTo(800);
    assertThat(image.height()).as("height() should return the result height").isEqualTo(600);
    assertThat(image.encodings())
        .as("encodings() should return the produced encodings in order")
        .containsExactly(jpeg, avif);
  }

  @Test
  void copiesEncodingsDefensively() {
    List<Encoded> encodings = new ArrayList<>(List.of(anEncoding()));
    ScaledImage image = ScaledImage.of(800, 600, encodings);

    encodings.clear();

    assertThat(image.encodings())
        .as("encodings should be copied defensively so later mutation of the input has no effect")
        .hasSize(1);
  }
}
