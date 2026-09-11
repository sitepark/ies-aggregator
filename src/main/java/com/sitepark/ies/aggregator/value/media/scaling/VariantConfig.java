package com.sitepark.ies.aggregator.value.media.scaling;

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * The scaling configuration of one named image variant (e.g. {@code teaser}, {@code kachel}), looked
 * up by name via {@link com.sitepark.ies.aggregator.port.VariantConfigProvider}.
 *
 * <p>The {@code fitMode} decides how a rendition reaches its box, spelled with the external names of
 * {@link FitMode}: {@code "cover"} crops to the configured {@code aspectRatio}, {@code "contain"}
 * only scales down until the image fits into the box, {@code "pad"} and {@code "coverOrPad"}
 * additionally fill the box with a background color. An omitted {@code fitMode} means {@code
 * "contain"}, so nothing is cropped unless the configuration asks for it.
 *
 * <p>A configuration is written by an editor of the project configuration, not by a program, which
 * is why it speaks in tokens rather than in types: {@code "3x2"} for an aspect ratio, {@code "avif"}
 * for a format. The {@code of(...)} factories are where those tokens become values — an adapter
 * reading the configuration hands over what it found and gets a validated variant back, whatever
 * format the configuration itself is stored in:
 *
 * <pre>{@code
 * {
 *   "fitMode": "cover",
 *   "aspectRatio": "3x2",
 *   "formats": [ {"type": "avif"}, {"type": "webp"}, {"type": "jpeg", "default": true} ],
 *   "srcset":  [ {"width": 640}, {"width": 360} ],
 *   "sizes":   [ {"mediaQuery": "(min-width: 640px)", "displaySize": "100vw"},
 *                {"displaySize": "100vw"} ]
 * }
 * }</pre>
 *
 * @param fitMode how a rendition reaches its box
 * @param aspectRatio the output aspect ratio, or {@code null} to keep the source ratio
 * @param formats the output formats to produce, in the order they are offered to the browser
 * @param srcset the widths to produce
 * @param sizes the HTML {@code sizes} entries; output only, they do not influence scaling
 */
public record VariantConfig(
    FitMode fitMode,
    @Nullable AspectRatio aspectRatio,
    List<FormatConfig> formats,
    List<SrcSetConfig> srcset,
    List<SizeConfig> sizes) {

  public VariantConfig {
    formats = List.copyOf(formats);
    srcset = List.copyOf(srcset);
    sizes = List.copyOf(sizes);
  }

  /**
   * Creates a variant configuration from a possibly incomplete one: an omitted block arrives as
   * {@code null} and each list is normalized to an empty list.
   *
   * @param fitMode the external fit-mode name, or {@code null} for {@link FitMode#CONTAIN}
   * @param aspectRatio the aspect ratio in the compact {@code "3x2"} notation, or {@code null} to
   *     keep the source ratio
   * @param formats the configured output formats, or {@code null} if none are configured
   * @param srcset the configured widths, or {@code null} if none are configured
   * @param sizes the configured {@code sizes} entries, or {@code null} if none are configured
   * @throws IllegalArgumentException if a token cannot be resolved
   */
  public static VariantConfig of(
      @Nullable String fitMode,
      @Nullable String aspectRatio,
      @Nullable List<FormatConfig> formats,
      @Nullable List<SrcSetConfig> srcset,
      @Nullable List<SizeConfig> sizes) {
    return new VariantConfig(
        parseFitMode(fitMode),
        parseAspectRatio(aspectRatio),
        formats != null ? formats : List.of(),
        srcset != null ? srcset : List.of(),
        sizes != null ? sizes : List.of());
  }

  /**
   * Returns the format used for the classic {@code <img>} fallback: the one flagged {@code default},
   * or — if none is flagged — the last configured one, as the {@code <source>} order lists formats
   * by descending preference.
   *
   * @return the fallback format, or empty if no format is configured
   */
  public Optional<FormatConfig> defaultFormat() {
    Optional<FormatConfig> flagged =
        this.formats.stream().filter(FormatConfig::isDefault).reduce((first, last) -> last);
    if (flagged.isPresent()) {
      return flagged;
    }
    return this.formats.isEmpty() ? Optional.empty() : Optional.of(this.formats.getLast());
  }

  /**
   * Resolves the fit mode from the external name of {@link FitMode} (e.g. {@code "cover"}),
   * defaulting to {@link FitMode#CONTAIN} when none is configured.
   *
   * <p>The string is resolved explicitly, so the result does not depend on how the adapter that read
   * the configuration happens to map enums.
   */
  private static FitMode parseFitMode(@Nullable String value) {
    if (value == null || value.isBlank()) {
      return FitMode.CONTAIN;
    }
    for (FitMode fitMode : FitMode.values()) {
      if (fitMode.getName().equals(value)) {
        return fitMode;
      }
    }
    throw new IllegalArgumentException("unknown fit mode: " + value);
  }

  /** Parses the compact {@code "3x2"} notation used by this configuration model. */
  private static @Nullable AspectRatio parseAspectRatio(@Nullable String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String[] parts = value.split("x", -1);
    if (parts.length != 2) {
      throw new IllegalArgumentException("invalid aspect ratio: " + value);
    }
    try {
      return AspectRatio.of(
          Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()), false);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("invalid aspect ratio: " + value, e);
    }
  }

  /**
   * One output format of a variant.
   *
   * @param type the format to produce
   * @param isDefault whether this format provides the {@code <img>} fallback
   */
  public record FormatConfig(Format type, boolean isDefault) {

    /**
     * Creates a format entry, resolving the configuration token (e.g. {@code "avif"}).
     *
     * @param type the external format name
     * @param isDefault whether this format is flagged as the fallback, or {@code null} if the flag
     *     is absent
     * @throws IllegalArgumentException if the format name is unknown
     */
    public static FormatConfig of(String type, @Nullable Boolean isDefault) {
      return new FormatConfig(parseFormat(type), isDefault != null && isDefault);
    }

    private static Format parseFormat(String type) {
      for (Format format : Format.values()) {
        if (format.getName().equals(type)) {
          return format;
        }
      }
      throw new IllegalArgumentException("unknown image format: " + type);
    }
  }

  /**
   * One entry of the {@code srcset}: a width to produce.
   *
   * @param width the requested width in pixels
   */
  public record SrcSetConfig(int width) {

    /**
     * Creates a srcset entry.
     *
     * @param width the requested width in pixels
     */
    public static SrcSetConfig of(int width) {
      return new SrcSetConfig(width);
    }
  }

  /**
   * One entry of the HTML {@code sizes} attribute.
   *
   * @param mediaQuery the media condition, or {@code null} for the trailing default entry
   * @param displaySize the display size within that condition (e.g. {@code "100vw"})
   */
  public record SizeConfig(@Nullable String mediaQuery, String displaySize) {

    /**
     * Creates a sizes entry.
     *
     * @param mediaQuery the media condition, or {@code null} for the trailing default entry
     * @param displaySize the display size within that condition
     */
    public static SizeConfig of(@Nullable String mediaQuery, String displaySize) {
      return new SizeConfig(mediaQuery, displaySize);
    }
  }
}
