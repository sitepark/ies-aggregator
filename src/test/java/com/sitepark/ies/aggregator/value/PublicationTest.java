package com.sitepark.ies.aggregator.value;

import static org.assertj.core.api.Assertions.assertThat;

import com.sitepark.ies.aggregator.value.uri.Uri;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class PublicationTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(Publication.class)
        .withPrefabValues(Uri.class, Uri.of("https://a.example/"), Uri.of("https://b.example/"))
        .verify();
  }

  @Test
  void exposesAllComponents() {
    Uri uri = Uri.of("https://example.com/page");
    Publication publication = new Publication(42, "/content/page", uri);

    assertThat(publication.id()).as("id() should return the constructor argument").isEqualTo(42);
    assertThat(publication.resourcePath())
        .as("resourcePath() should return the constructor argument")
        .isEqualTo("/content/page");
    assertThat(publication.uri()).as("uri() should return the constructor argument").isSameAs(uri);
  }
}
