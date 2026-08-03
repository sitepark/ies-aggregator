package com.sitepark.ies.aggregator.value.text;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class TranslatableTextTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(TranslatableText.class)
        .withNonnullFields("sourceText", "format")
        .verify();
  }

  @Test
  void nullSourceIsNormalizedToEmptyText() {
    assertThat(TranslatableText.of(null).getSourceText())
        .as("null source text should be normalized to empty string")
        .isEmpty();
  }

  @Test
  void blankSourceIsNormalizedToEmptyText() {
    assertThat(TranslatableText.of("   ").getSourceText())
        .as("blank source text should be normalized to empty string")
        .isEmpty();
  }

  @Test
  void nonBlankSourceIsKeptAsIs() {
    assertThat(TranslatableText.of("hello").getSourceText())
        .as("non-blank source text should be kept unchanged")
        .isEqualTo("hello");
  }

  @Test
  void defaultFormatIsText() {
    assertThat(TranslatableText.of("hi").getFormat())
        .as("of(String) should default to Format.TEXT")
        .isEqualTo(TranslatableText.Format.TEXT);
  }

  @Test
  void formatCanBeOverridden() {
    assertThat(TranslatableText.of("<b>x</b>", TranslatableText.Format.HTML).getFormat())
        .as("of(String, Format) should use the supplied format")
        .isEqualTo(TranslatableText.Format.HTML);
  }

  @Test
  void toStringAlwaysReturnsSourceText() {
    TranslatableText text = TranslatableText.of("hello");

    assertThat(text.toString())
        .as("toString() should always return the untranslated source text")
        .isEqualTo("hello");
  }

  @Test
  void copyHasIndependentIdentityAsTranslationKey() {
    TranslatableText original = TranslatableText.of("foo");
    TranslatableText copy = original.copy();

    Translations table =
        Translations.fromIndexed(List.of(original, copy), List.of("orig", "copied"), "de");

    assertThat(table.translationFor(original))
        .as("Original and copy are distinct keys, so the original keeps its own translation")
        .isEqualTo("orig");
    assertThat(table.translationFor(copy))
        .as("Original and copy are distinct keys, so the copy keeps its own translation")
        .isEqualTo("copied");
  }

  @Test
  void valueEqualTextsRemainSeparateTranslationKeys() {
    TranslatableText first = TranslatableText.of("foo");
    TranslatableText second = TranslatableText.of("foo");

    Translations table = Translations.fromIndexed(List.of(first, second), List.of("A", "B"), "de");

    assertThat(first)
        .as("Two separately created texts with the same source are value-equal")
        .isEqualTo(second);
    assertThat(table.translationFor(first))
        .as("Value-equal texts are still distinct table keys, so the first keeps its translation")
        .isEqualTo("A");
    assertThat(table.translationFor(second))
        .as("Value-equal texts are still distinct table keys, so the second keeps its translation")
        .isEqualTo("B");
  }

  @Test
  void copyIsValueEqualButADistinctInstance() {
    TranslatableText original = TranslatableText.of("<b>x</b>", TranslatableText.Format.HTML);

    TranslatableText copy = original.copy();

    assertThat(copy)
        .as("A copy carries the same source text and format as its origin")
        .isEqualTo(original);
    assertThat(copy)
        .as("A copy is a distinct instance, which is what makes it an independent translation slot")
        .isNotSameAs(original);
  }

  @Test
  void plainProducesPlainTextWithSameSource() {
    PlainText plain = TranslatableText.of("hello").plain();

    assertThat(plain.toString())
        .as("plain() should produce a PlainText carrying the same source text")
        .isEqualTo("hello");
  }
}
