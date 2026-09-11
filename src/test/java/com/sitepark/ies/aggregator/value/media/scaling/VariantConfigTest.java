package com.sitepark.ies.aggregator.value.media.scaling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sitepark.ies.aggregator.value.media.scaling.VariantConfig.FormatConfig;
import com.sitepark.ies.aggregator.value.media.scaling.VariantConfig.SizeConfig;
import com.sitepark.ies.aggregator.value.media.scaling.VariantConfig.SrcSetConfig;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Covers the configuration language of a variant: which tokens an editor of the project
 * configuration may write, and what they mean.
 */
class VariantConfigTest {

  /** A configuration with nothing set, as an adapter hands it over for an empty block. */
  private static VariantConfig empty() {
    return VariantConfig.of(null, null, null, null, null);
  }

  @Test
  void readsTheFullConfiguration() {
    VariantConfig variant =
        VariantConfig.of(
            "cover",
            "3x2",
            List.of(FormatConfig.of("avif", null), FormatConfig.of("jpeg", true)),
            List.of(SrcSetConfig.of(640), SrcSetConfig.of(360)),
            List.of(SizeConfig.of("(min-width: 640px)", "100vw"), SizeConfig.of(null, "50vw")));

    assertThat(variant.fitMode())
        .as("the external fit-mode name \"cover\" should be read from the configuration")
        .isEqualTo(FitMode.COVER);
    assertThat(variant.aspectRatio())
        .as("the compact \"3x2\" notation should be parsed into an aspect ratio")
        .isEqualTo(AspectRatio.of(3, 2, false));
    assertThat(variant.formats())
        .as("formats should be kept in configuration order, with the default flag")
        .containsExactly(new FormatConfig(Format.AVIF, false), new FormatConfig(Format.JPEG, true));
    assertThat(variant.srcset())
        .as("srcset widths should be kept in configuration order")
        .extracting(SrcSetConfig::width)
        .containsExactly(640, 360);
    assertThat(variant.sizes())
        .as("sizes should be kept in configuration order, the last one without a media query")
        .containsExactly(
            new SizeConfig("(min-width: 640px)", "100vw"), new SizeConfig(null, "50vw"));
  }

  @Test
  void appliesDefaultsForAnEmptyConfiguration() {
    VariantConfig variant = empty();

    assertThat(variant.fitMode())
        .as("an omitted fit mode should default to contain, so nothing is cropped")
        .isEqualTo(FitMode.CONTAIN);
    assertThat(variant.aspectRatio())
        .as("without an aspect ratio the source ratio is kept")
        .isNull();
    assertThat(variant.formats()).as("omitted formats should become an empty list").isEmpty();
    assertThat(variant.srcset()).as("an omitted srcset should become an empty list").isEmpty();
    assertThat(variant.sizes()).as("omitted sizes should become an empty list").isEmpty();
  }

  @Test
  void rejectsAnUnparsableAspectRatio() {
    assertThatThrownBy(() -> VariantConfig.of(null, "3zu2", null, null, null))
        .as("an aspect ratio that is not <x>x<y> should be rejected")
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("3zu2");
  }

  @Test
  void rejectsAnUnknownFormat() {
    assertThatThrownBy(() -> FormatConfig.of("heic", null))
        .as("a format the scaler cannot produce should be rejected as a configuration error")
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("heic");
  }

  @Test
  void readsEveryFitModeByItsExternalName() {
    assertThat(VariantConfig.of("contain", null, null, null, null).fitMode())
        .as("\"contain\" should be read as the contain fit mode")
        .isEqualTo(FitMode.CONTAIN);
    assertThat(VariantConfig.of("pad", null, null, null, null).fitMode())
        .as("\"pad\" should be read as the pad fit mode")
        .isEqualTo(FitMode.PAD);
    assertThat(VariantConfig.of("coverOrPad", null, null, null, null).fitMode())
        .as("\"coverOrPad\" should be read as the cover-or-pad fit mode")
        .isEqualTo(FitMode.COVER_OR_PAD);
  }

  @Test
  void rejectsAnUnknownFitMode() {
    assertThatThrownBy(() -> VariantConfig.of("stretch", null, null, null, null))
        .as("a fit mode the scaler does not know should be rejected as a configuration error")
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("stretch");
  }

  @Test
  void theLastFlaggedFormatProvidesTheFallback() {
    VariantConfig variant =
        VariantConfig.of(
            null,
            null,
            List.of(FormatConfig.of("avif", true), FormatConfig.of("jpeg", true)),
            null,
            null);

    assertThat(variant.defaultFormat())
        .as("when several formats are flagged, the last one wins")
        .contains(new FormatConfig(Format.JPEG, true));
  }

  @Test
  void withoutAFlagTheLastConfiguredFormatProvidesTheFallback() {
    VariantConfig variant =
        VariantConfig.of(
            null,
            null,
            List.of(FormatConfig.of("avif", null), FormatConfig.of("jpeg", null)),
            null,
            null);

    assertThat(variant.defaultFormat())
        .as(
            "the source order lists formats by descending preference, so the last one is the"
                + " fallback")
        .contains(new FormatConfig(Format.JPEG, false));
  }

  @Test
  void withoutAnyFormatThereIsNoFallback() {
    assertThat(empty().defaultFormat())
        .as("a variant without formats has no fallback format")
        .isEmpty();
  }

  @Test
  void copiesListsDefensively() {
    List<SrcSetConfig> srcset = new ArrayList<>(List.of(SrcSetConfig.of(640)));
    VariantConfig variant = new VariantConfig(FitMode.CONTAIN, null, List.of(), srcset, List.of());

    srcset.clear();

    assertThat(variant.srcset())
        .as("srcset should be copied defensively so later mutation of the input has no effect")
        .hasSize(1);
  }
}
