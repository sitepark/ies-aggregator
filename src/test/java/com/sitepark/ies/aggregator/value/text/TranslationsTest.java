package com.sitepark.ies.aggregator.value.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class TranslationsTest {

  @Test
  void fromIndexedMapsEachNodeToTheTargetAtTheSameIndex() {
    TranslatableText first = TranslatableText.of("one");
    TranslatableText second = TranslatableText.of("two");

    Translations table =
        Translations.fromIndexed(List.of(first, second), List.of("eins", "zwei"), "de");

    assertThat(table.translationFor(first))
        .as("First node should map to the target at index 0")
        .isEqualTo("eins");
    assertThat(table.translationFor(second))
        .as("Second node should map to the target at index 1")
        .isEqualTo("zwei");
  }

  @Test
  void fromIndexedRejectsMismatchedSizes() {
    TranslatableText text = TranslatableText.of("one");

    assertThatThrownBy(() -> Translations.fromIndexed(List.of(text), List.of("eins", "zwei"), "de"))
        .as("fromIndexed should reject differing node/target counts to protect the index bridge")
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void translationForFallsBackToSourceWhenTextIsNotRegistered() {
    TranslatableText registered = TranslatableText.of("known");
    TranslatableText unregistered = TranslatableText.of("unknown");
    Translations table = Translations.fromIndexed(List.of(registered), List.of("bekannt"), "de");

    assertThat(table.translationFor(unregistered))
        .as("An unregistered text should fall back to its own source text")
        .isEqualTo("unknown");
  }

  @Test
  void translationForUsesIdentityNotValueEquality() {
    TranslatableText registered = TranslatableText.of("same");
    TranslatableText sameValueDifferentInstance = TranslatableText.of("same");
    Translations table = Translations.fromIndexed(List.of(registered), List.of("translated"), "de");

    assertThat(table.translationFor(sameValueDifferentInstance))
        .as("A distinct instance with equal source must not pick up the registered translation")
        .isEqualTo("same");
  }

  @Test
  void sourceTableHasNoTargetLanguage() {
    assertThat(Translations.SOURCE.targetLang())
        .as("SOURCE should have no target language so URIs render without a prefix")
        .isNull();
  }

  @Test
  void sourceTableReturnsSourceText() {
    TranslatableText text = TranslatableText.of("hello");

    assertThat(Translations.SOURCE.translationFor(text))
        .as("SOURCE should return the untranslated source text for any text")
        .isEqualTo("hello");
  }
}
