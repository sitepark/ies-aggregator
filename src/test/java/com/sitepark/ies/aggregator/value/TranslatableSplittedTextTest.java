package com.sitepark.ies.aggregator.value;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class TranslatableSplittedTextTest {

  @Test
  void emptyInstanceHasNoTranslatableTexts() {
    assertThat(new TranslatableSplitText().getTranslatableTextList())
        .as("An empty instance should return an empty translatable text list")
        .isEmpty();
  }

  @Test
  void getTranslatableTextListReturnsOnlyTranslatableSegmentsInOrder() {
    TranslatableSplitText splitted = new TranslatableSplitText();
    TranslatableText first = TranslatableText.of("one");
    TranslatableText second = TranslatableText.of("two");
    splitted.add("prefix-");
    splitted.add(first);
    splitted.add(" middle ");
    splitted.add(second);

    assertThat(splitted.getTranslatableTextList())
        .as("Only TranslatableText segments should be returned, preserving insertion order")
        .containsExactly(first, second);
  }

  @Test
  void translatableSegmentInstancesAreReturnedAsIs() {
    TranslatableSplitText splitted = new TranslatableSplitText();
    TranslatableText embedded = TranslatableText.of("one");
    splitted.add(embedded);

    assertThat(splitted.getTranslatableTextList().get(0))
        .as("Returned TranslatableText should be the exact same instance, not a copy")
        .isSameAs(embedded);
  }

  @Test
  void toStringConcatenatesSourceSegmentsInOrder() {
    TranslatableSplitText splitted = new TranslatableSplitText();
    splitted.add("Hello, ");
    splitted.add(TranslatableText.of("world"));
    splitted.add("!");

    assertThat(splitted.toString())
        .as("toString() should concatenate all segments in their source language in order")
        .isEqualTo("Hello, world!");
  }

  @Test
  void renderTranslatesTranslatableSegmentsAndKeepsPlainSegments() {
    TranslatableSplitText splitted = new TranslatableSplitText();
    TranslatableText embedded = TranslatableText.of("world");
    splitted.add("Hello, ");
    splitted.add(embedded);
    splitted.add("!");
    Translations de = Translations.fromIndexed(List.of(embedded), List.of("Welt"), "de");

    assertThat(splitted.render(de))
        .as("render() should translate TranslatableText segments and keep plain segments verbatim")
        .isEqualTo("Hello, Welt!");
  }

  @Test
  void renderFallsBackToSourceForUntranslatedSegments() {
    TranslatableSplitText splitted = new TranslatableSplitText();
    splitted.add("Hello, ");
    splitted.add(TranslatableText.of("world"));

    assertThat(splitted.render(Translations.SOURCE))
        .as("render() should fall back to the source text when no translation is registered")
        .isEqualTo("Hello, world");
  }

  @Test
  void collectionConstructorPrePopulatesSegments() {
    TranslatableText embedded = TranslatableText.of("x");
    TranslatableSplitText splitted = new TranslatableSplitText(Arrays.asList("a-", embedded));

    assertThat(splitted.toString())
        .as("Constructor with a Collection should pre-populate segments in given order")
        .isEqualTo("a-x");
  }

  @Test
  void nullCollectionConstructorProducesEmptyInstance() {
    assertThat(new TranslatableSplitText(null).toString())
        .as("Constructor accepting null should produce an empty instance with empty toString")
        .isEmpty();
  }
}
