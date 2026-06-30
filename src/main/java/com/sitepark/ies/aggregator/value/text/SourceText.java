package com.sitepark.ies.aggregator.value.text;

/**
 * A source text to be translated, carrying its {@link TranslatableText.Format Format} so a
 * translation service can handle plain text and HTML differently.
 *
 * <p>Extract these from the ordered list returned by {@code TranslatableTextCollector.collect}; the
 * translated strings come back in the same order and are mapped back via {@link
 * Translations#fromIndexed}.
 *
 * @param text the source text
 * @param format the text format
 */
public record SourceText(String text, TranslatableText.Format format) {

  /**
   * Creates a {@code SourceText} from a collected {@link TranslatableText}.
   *
   * @param translatableText the collected text
   * @return the source text with its format
   */
  public static SourceText of(TranslatableText translatableText) {
    return new SourceText(translatableText.getSourceText(), translatableText.getFormat());
  }
}
