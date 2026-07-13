package com.sitepark.ies.aggregator.value.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

class TextTest {

  @Test
  void ofProducesNonTranslatablePlainText() {
    Text text = Text.of("hello");

    assertThat(text)
        .as("Text.of() should produce a non-translatable PlainText")
        .isInstanceOf(PlainText.class);
  }

  @Test
  void translatableTextIsAlsoAText() {
    assertThat(TranslatableText.of("hello"))
        .as("TranslatableText should be usable wherever a Text is expected")
        .isInstanceOf(Text.class);
  }

  @Test
  void orElseWithValueReturnsFallbackWhenEmpty() {
    Text fallback = Text.of("fallback");

    assertThat(PlainText.EMPTY.orElse(fallback))
        .as("An empty text should be replaced by the eager fallback")
        .isSameAs(fallback);
  }

  @Test
  void orElseWithValueReturnsThisWhenNonEmpty() {
    Text text = Text.of("value");

    assertThat(text.orElse(Text.of("fallback")))
        .as("A non-empty text should keep its own value, ignoring the eager fallback")
        .isSameAs(text);
  }

  @Test
  void orElseWithSupplierReturnsFallbackWhenEmpty() {
    assertThat(PlainText.EMPTY.orElse(() -> Text.of("fallback")))
        .as("An empty text should be replaced by the supplied fallback")
        .isEqualTo(Text.of("fallback"));
  }

  @Test
  void orElseWithSupplierReturnsThisWithoutEvaluatingFallbackWhenNonEmpty() {
    Text text = Text.of("value");

    Text result =
        text.orElse(() -> fail("Fallback supplier must not be evaluated for a non-empty text"));

    assertThat(result).as("A non-empty text should keep its own value").isSameAs(text);
  }

  @Test
  void orElseChainedWithTranslatableProducesTranslatableText() {
    Text label = PlainText.EMPTY.orElse(() -> Text.of("headline")).translatable();

    assertThat(label)
        .as("A fallback text made translatable should be a TranslatableText")
        .isInstanceOf(TranslatableText.class);
  }
}
