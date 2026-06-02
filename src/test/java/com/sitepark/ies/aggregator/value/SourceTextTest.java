package com.sitepark.ies.aggregator.value;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SourceTextTest {

  @Test
  void ofCarriesTextAndFormatFromTranslatableText() {
    TranslatableText text = TranslatableText.of("<b>x</b>", TranslatableText.Format.HTML);

    SourceText source = SourceText.of(text);

    assertThat(source.text())
        .as("SourceText should carry the source text of the TranslatableText")
        .isEqualTo("<b>x</b>");
    assertThat(source.format())
        .as("SourceText should carry the format of the TranslatableText")
        .isEqualTo(TranslatableText.Format.HTML);
  }
}
