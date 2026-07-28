package com.sitepark.ies.aggregator.output.format;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class PlainCodeTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(PlainCode.class).withNonnullFields("content").verify();
  }

  @Test
  void factoryRejectsNullContent() {
    assertThatThrownBy(() -> PlainCode.of(null))
        .as("of() should reject null content with NullPointerException")
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void codeReturnsContentVerbatim() {
    assertThat(PlainCode.of("  foo()  ").code())
        .as("code() should return the content without trimming or escaping")
        .isEqualTo("  foo()  ");
  }

  @Test
  void toStringReturnsContent() {
    assertThat(PlainCode.of("foo").toString())
        .as("toString() should return the content")
        .isEqualTo("foo");
  }

  @Test
  void emptyContentIsEmpty() {
    assertThat(PlainCode.of("").isEmpty())
        .as("PlainCode with empty content should report empty")
        .isTrue();
  }

  @Test
  void nonEmptyContentIsNotEmpty() {
    assertThat(PlainCode.of("foo").isEmpty())
        .as("PlainCode with content should not report empty")
        .isFalse();
  }
}
