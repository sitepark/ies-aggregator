package com.sitepark.ies.aggregator.value.text;

import static org.assertj.core.api.Assertions.assertThat;

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
}
