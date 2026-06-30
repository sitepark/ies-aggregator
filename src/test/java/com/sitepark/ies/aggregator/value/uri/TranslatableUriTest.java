package com.sitepark.ies.aggregator.value.uri;

import static org.assertj.core.api.Assertions.assertThat;

import com.sitepark.ies.aggregator.value.text.TranslatableText;
import com.sitepark.ies.aggregator.value.text.Translations;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;

class TranslatableUriTest {

  @Test
  void pathSegmentsAreExposedAsTranslatableTexts() {
    TranslatableUri uri = TranslatableUri.of(URI.create("https://example.com/foo/bar"));

    List<TranslatableText> texts = uri.getTranslatableTextList();

    assertThat(texts)
        .as("Path segments should be exposed as TranslatableText source texts in order")
        .extracting(TranslatableText::getSourceText)
        .containsExactly("foo", "bar");
  }

  @Test
  void emptyPathProducesNoTranslatableTexts() {
    TranslatableUri uri = TranslatableUri.of(URI.create("https://example.com/"));

    assertThat(uri.getTranslatableTextList())
        .as("An empty path should produce an empty translatable text list")
        .isEmpty();
  }

  @Test
  void uriWithoutPathProducesNoTranslatableTexts() {
    TranslatableUri uri = TranslatableUri.of(URI.create("mailto:test@example.com"));

    assertThat(uri.getTranslatableTextList())
        .as("A URI without a path should produce an empty translatable text list, not crash")
        .isEmpty();
  }

  @Test
  void trailingNumericIdSuffixIsStrippedFromSegments() {
    TranslatableUri uri = TranslatableUri.of(URI.create("https://example.com/page/article-42"));

    assertThat(uri.getTranslatableTextList())
        .as("Trailing -<id> suffix should be stripped from the last path segment")
        .extracting(TranslatableText::getSourceText)
        .containsExactly("page", "article");
  }

  @Test
  void micrositeSegmentIsNotExposedAsTranslatable() {
    TranslatableUri uri = TranslatableUri.of(URI.create("https://example.com/microsite/shop/page"));

    assertThat(uri.getTranslatableTextList())
        .as("Microsite name segment should be preserved and excluded from translatable texts")
        .extracting(TranslatableText::getSourceText)
        .containsExactly("page");
  }

  @Test
  void renderAddsTargetLangPrefixAndTranslatedSegments() {
    TranslatableUri uri = TranslatableUri.of(URI.create("https://example.com/foo/bar"));
    Translations de =
        Translations.fromIndexed(uri.getTranslatableTextList(), List.of("haus", "garten"), "de");

    assertThat(uri.render(de))
        .as("Rendered URI should contain the target language prefix and translated path segments")
        .isEqualTo("https://example.com/de/haus/garten");
  }

  @Test
  void renderPreservesIdSuffixAfterTranslation() {
    TranslatableUri uri = TranslatableUri.of(URI.create("https://example.com/article-42"));
    Translations de =
        Translations.fromIndexed(uri.getTranslatableTextList(), List.of("artikel"), "de");

    assertThat(uri.render(de))
        .as("Trailing -<id> suffix should be re-attached after the translated last segment")
        .isEqualTo("https://example.com/de/artikel-42");
  }

  @Test
  void renderEmitsMicrositeBlockBeforeTranslatedSegments() {
    TranslatableUri uri = TranslatableUri.of(URI.create("https://example.com/microsite/shop/page"));
    Translations de =
        Translations.fromIndexed(uri.getTranslatableTextList(), List.of("seite"), "de");

    assertThat(uri.render(de))
        .as("Microsite block should be emitted between target language and translated segments")
        .isEqualTo("https://example.com/de/microsite/shop/seite");
  }

  @Test
  void renderHomepageWithSlashWhenNoSegmentsAndNoMicrosite() {
    TranslatableUri uri = TranslatableUri.of(URI.create("https://example.com/"));
    Translations de = Translations.fromIndexed(List.of(), List.of(), "de");

    assertThat(uri.render(de))
        .as("Homepage should be rendered as /<lang>/ when no segments and no microsite")
        .isEqualTo("https://example.com/de/");
  }

  @Test
  void renderUriForReturnsPlainUriWithTranslationApplied() {
    TranslatableUri uri = TranslatableUri.of(URI.create("https://example.com/foo"));
    Translations de =
        Translations.fromIndexed(uri.getTranslatableTextList(), List.of("haus"), "de");

    PlainUri translated = uri.renderUriFor(de);

    assertThat(translated)
        .as("renderUriFor() should return a plain PlainUri, not the TranslatableUri itself")
        .isExactlyInstanceOf(PlainUri.class);
    assertThat(translated.toString())
        .as("renderUriFor() should reflect the applied translation in the path")
        .isEqualTo("https://example.com/de/haus");
  }

  @Test
  void spaceInTranslationIsReplacedWithUnderscore() {
    TranslatableUri uri = TranslatableUri.of(URI.create("https://example.com/page"));
    Translations de =
        Translations.fromIndexed(uri.getTranslatableTextList(), List.of("neue seite"), "de");

    assertThat(uri.render(de))
        .as("Spaces in translated segments should be replaced with underscores in the URI path")
        .isEqualTo("https://example.com/de/neue_seite");
  }

  @Test
  void toStringRendersSourceWithoutLangPrefix() {
    TranslatableUri uri = TranslatableUri.of(URI.create("https://example.com/foo/bar"));

    assertThat(uri.toString())
        .as("toString() should render the source language with no language prefix")
        .isEqualTo("https://example.com/foo/bar");
  }

  @Test
  void copyDoesNotShareTranslationSegmentsWithOriginal() {
    TranslatableUri original = TranslatableUri.of(URI.create("/foo"));
    TranslatableUri copy = original.toAbsolutePathReference();

    Translations de =
        Translations.fromIndexed(copy.getTranslatableTextList(), List.of("changed"), "de");

    assertThat(original.getTranslatableTextList().get(0).toString())
        .as("Translating a copy's path segment must not affect the original's segment")
        .isEqualTo("foo");
    assertThat(de.translationFor(original.getTranslatableTextList().get(0)))
        .as("Original segment is a distinct key, so it is not translated by the copy's table")
        .isEqualTo("foo");
  }

  @Test
  void plainReturnsUntranslatedSourceUri() {
    TranslatableUri uri = TranslatableUri.of(URI.create("https://example.com/foo/bar"));

    PlainUri plain = uri.plain();

    assertThat(plain.toString())
        .as("plain() should return the untranslated source URI without a language prefix")
        .isEqualTo("https://example.com/foo/bar");
  }
}
