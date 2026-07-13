package com.sitepark.ies.aggregator.value.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A mixed-content value object that interleaves plain strings and {@link TranslatableText}
 * segments.
 *
 * <p>{@link #render(Translations)} concatenates all segments in order, looking each {@link
 * TranslatableText} segment up in the given table. {@link #toString()} renders the source language.
 * The {@link TranslatableContainer} implementation returns only the {@link TranslatableText}
 * segments so they can be collected for translation.
 *
 * <p>Because this container holds mutable, identity-keyed {@link TranslatableText} segments (see
 * {@link TranslatableText}), {@code equals}/{@code hashCode} are deliberately
 * <strong>identity-based</strong> (reference equality), not value-based: value-based equality would
 * be semantically wrong here.
 */
public class TranslatableSplitText implements TranslatableContainer {
  private final List<Object> splittedText = new ArrayList<>();

  /** Creates an empty instance. */
  public TranslatableSplitText() {}

  /**
   * Creates an instance pre-populated with the given segments.
   *
   * @param splittedText the initial segments; may be {@code null} (treated as empty)
   */
  public TranslatableSplitText(Collection<?> splittedText) {
    if (splittedText != null) {
      this.splittedText.addAll(splittedText);
    }
  }

  /**
   * Appends a segment to this value.
   *
   * @param o the segment to add; may be a {@link TranslatableText} or any other {@link Object}
   */
  public void add(Object o) {
    this.splittedText.add(o);
  }

  @Override
  public List<TranslatableText> getTranslatableTextList() {
    List<TranslatableText> translatableTextList = new ArrayList<>();
    for (Object o : this.splittedText) {
      if (o instanceof TranslatableText text) {
        translatableTextList.add(text);
      }
    }
    return translatableTextList;
  }

  /**
   * Concatenates all segments, applying the given translations to {@link TranslatableText}
   * segments.
   *
   * @param translations the translation table (use {@link Translations#SOURCE} for the source
   *     language)
   * @return the rendered string
   */
  @Override
  public String render(Translations translations) {
    StringBuilder sb = new StringBuilder();
    for (Object o : this.splittedText) {
      if (o instanceof TranslatableText text) {
        sb.append(translations.translationFor(text));
      } else {
        sb.append(o);
      }
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    return this.render(Translations.SOURCE);
  }

  @Override
  public boolean equals(Object o) {
    // Identity-based by design: see class Javadoc.
    return this == o;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }
}
