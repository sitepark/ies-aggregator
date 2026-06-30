package com.sitepark.ies.aggregator.value.text;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class TranslatableTextTest {

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
  void copyCarriesSameSourceTextAndFormat() {
    TranslatableText original = TranslatableText.of("<b>x</b>", TranslatableText.Format.HTML);

    TranslatableText copy = original.copy();

    assertThat(copy.getSourceText())
        .as("Copy should carry the same source text")
        .isEqualTo("<b>x</b>");
    assertThat(copy.getFormat())
        .as("Copy should carry the same format")
        .isEqualTo(TranslatableText.Format.HTML);
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
  void plainProducesPlainTextWithSameSource() {
    PlainText plain = TranslatableText.of("hello").plain();

    assertThat(plain.toString())
        .as("plain() should produce a PlainText carrying the same source text")
        .isEqualTo("hello");
  }
}
