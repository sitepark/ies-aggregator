package com.sitepark.ies.aggregator.value.media.scaling;

import com.sitepark.ies.aggregator.value.NamedEnum;

/**
 * How a scaled image is fitted into its target size when it cannot fill it exactly.
 *
 * <p>Two rules always apply when scaling: an image is never enlarged and never distorted. As a
 * consequence a target size cannot always be reached with real image data, and this mode decides
 * what happens then. The four modes are the complete vocabulary the scaling engine offers for
 * reconciling a target size with a source image of a different aspect ratio.
 *
 * <p>This is the single fit vocabulary of the whole scaling stack, from the configuration a caller
 * reads down to the port boundary (see {@link com.sitepark.ies.aggregator.port.ImageScaler}).
 *
 * <p>The external name (see {@link #getName()}) is the stable spelling used in configuration; {@link
 * #toString()} returns the same name.
 */
public enum FitMode implements NamedEnum {

  /**
   * Crops the image to the target size, placing the crop by the focal point. Nothing is padded, so a
   * source too small to cover the target stays smaller than requested.
   */
  COVER("cover"),

  /**
   * Scales the image down until it fits entirely inside the target size, keeping the source aspect
   * ratio. Nothing is cropped and nothing is padded, so the result may be smaller than the target on
   * one axis.
   */
  CONTAIN("contain"),

  /**
   * Scales the image down to fit inside the target size and adds a background-colored frame, so the
   * target size is always reached exactly. Nothing is cropped.
   */
  PAD("pad"),

  /**
   * Crops like {@link #COVER} while the source can cover the target, and pads like {@link #PAD}
   * otherwise. The target size is always reached exactly.
   */
  COVER_OR_PAD("coverOrPad");

  private final String name;

  FitMode(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
