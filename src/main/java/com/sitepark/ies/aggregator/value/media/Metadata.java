package com.sitepark.ies.aggregator.value.media;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

/**
 * Descriptive metadata shared by every {@link Media} asset.
 *
 * <p>Sealed over {@link GenericMetadata} (for media without kind-specific descriptive data) and
 * {@link ImageMetadata} (adding image-specific data such as a {@link FocalPoint}).
 */
public sealed interface Metadata permits GenericMetadata, ImageMetadata {

  /** Returns the alternative text, or {@code null} if absent. */
  @Nullable String alternativeText();

  /** Returns the copyright notice, or {@code null} if absent. */
  @Nullable String copyright();

  /** Returns the title, or {@code null} if absent. */
  @Nullable String title();

  /** Returns the description, or {@code null} if absent. */
  @Nullable String description();

  /** Returns the modification timestamp, or {@code null} if unknown. */
  @Nullable Instant lastModified();
}
