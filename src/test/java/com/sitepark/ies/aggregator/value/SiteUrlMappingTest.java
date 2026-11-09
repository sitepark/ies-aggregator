package com.sitepark.ies.aggregator.value;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.Locale;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class SiteUrlMappingTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(SiteUrlMapping.class).verify();
  }

  @Test
  void exposesAllComponents() {
    SiteUrlMapping mapping =
        new SiteUrlMapping("/kontakt", "4711", UrlMappingMode.FORWARD, Locale.GERMANY);

    assertThat(mapping.path())
        .as("path() should return the constructor argument")
        .isEqualTo("/kontakt");
    assertThat(mapping.ownerId())
        .as("ownerId() should return the constructor argument")
        .isEqualTo("4711");
    assertThat(mapping.mode())
        .as("mode() should return the constructor argument")
        .isEqualTo(UrlMappingMode.FORWARD);
    assertThat(mapping.locale())
        .as("locale() should return the constructor argument")
        .isEqualTo(Locale.GERMANY);
  }

  @Test
  void localeMayBeAbsent() {
    SiteUrlMapping mapping =
        new SiteUrlMapping("/kontakt", "4711", UrlMappingMode.REDIRECT_PERMANENT, null);

    assertThat(mapping.locale()).as("a mapping for every language should have no locale").isNull();
  }

  @Test
  void rejectsNullPath() {
    assertThatNullPointerException()
        .as("a mapping without a path should be rejected")
        .isThrownBy(() -> new SiteUrlMapping(null, "1", UrlMappingMode.FORWARD, null));
  }

  @Test
  void rejectsNullOwnerId() {
    assertThatNullPointerException()
        .as("a mapping without an owner should be rejected")
        .isThrownBy(() -> new SiteUrlMapping("/a", null, UrlMappingMode.FORWARD, null));
  }

  @Test
  void rejectsNullMode() {
    assertThatNullPointerException()
        .as("a mapping without a mode should be rejected")
        .isThrownBy(() -> new SiteUrlMapping("/a", "1", null, null));
  }
}
