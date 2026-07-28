package com.sitepark.ies.aggregator.value.media.scaling;

import com.sitepark.ies.aggregator.value.NamedEnum;

/**
 * How a scaled image is fitted into a requested size when it cannot fill it exactly.
 *
 * <p>Two rules always apply when scaling: an image is never enlarged and never distorted. As a
 * consequence the requested size cannot always be reached. The padding mode controls what happens
 * then:
 *
 * <ul>
 *   <li>{@link #NO}: no filling; the image simply stays smaller than the requested size.
 *   <li>{@link #YES}: a frame in a configurable background color is added around the image so it
 *       reaches the requested size exactly.
 *   <li>{@link #FIT}: the image is cropped (honoring the focal point) until either width or height
 *       matches the requested value.
 * </ul>
 *
 * <p>The external name (see {@link #getName()}) is the stable, CMS-facing spelling used when reading
 * the value from configuration. {@link #toString()} returns the same external name.
 */
public enum Padding implements NamedEnum {

  /** No filling; the image may stay smaller than the requested size. */
  NO("no"),

  /** A background-colored frame is added so the image reaches the requested size exactly. */
  YES("yes"),

  /** The image is cropped until either width or height matches the requested value. */
  FIT("fit");

  private final String name;

  Padding(String name) {
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
