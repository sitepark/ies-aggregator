package com.sitepark.ies.aggregator.value.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class PlainTextTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(PlainText.class).withNonnullFields("text").verify();
  }

  @Test
  void nullValueIsRejected() {
    assertThatThrownBy(() -> PlainText.of(null))
        .as("Constructor should reject null text with NullPointerException")
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void toStringReturnsRawText() {
    assertThat(PlainText.of("hello").toString())
        .as("toString() should return the raw text content")
        .isEqualTo("hello");
  }

  @Test
  void surroundingWhitespaceIsStripped() {
    assertThat(PlainText.of("  hello  ").toString())
        .as("constructor should strip leading and trailing whitespace")
        .isEqualTo("hello");
  }

  @Test
  void valuesDifferingOnlyInSurroundingWhitespaceAreEqual() {
    assertThat(PlainText.of("  hello  "))
        .as("texts differing only in surrounding whitespace should be equal after trimming")
        .isEqualTo(PlainText.of("hello"));
  }

  @Test
  void emptyConstantHasEmptyToString() {
    assertThat(PlainText.EMPTY.toString())
        .as("EMPTY constant should render as the empty string")
        .isEmpty();
  }

  @Test
  void translatablePromotesToTranslatableTextWithSameSource() {
    TranslatableText translatable = PlainText.of("hello").translatable();

    assertThat(translatable.getSourceText())
        .as("translatable() should produce a TranslatableText with the same source text")
        .isEqualTo("hello");
  }

  @Test
  void translatableWithoutArgumentDefaultsToTextFormat() {
    assertThat(PlainText.of("hello").translatable().getFormat())
        .as("translatable() without format should default to Format.TEXT")
        .isEqualTo(TranslatableText.Format.TEXT);
  }

  @Test
  void translatableWithFormatUsesSuppliedFormat() {
    assertThat(PlainText.of("<b>bold</b>").translatable(TranslatableText.Format.HTML).getFormat())
        .as("translatable(Format) should use the supplied format")
        .isEqualTo(TranslatableText.Format.HTML);
  }
}
