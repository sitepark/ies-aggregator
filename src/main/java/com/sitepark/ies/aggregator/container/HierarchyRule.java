package com.sitepark.ies.aggregator.container;

import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * How an entry of a section container takes part in the hierarchy of sections; read from the
 * {@code aggregator} configuration of its section type, as the former aggregator does.
 *
 * @param newSection the entry opens a new section
 * @param sectionLevel the level of the opened section, or {@code null}
 * @param newSectionType the type of the opened section if it wraps the entry, or {@code null} if
 *     the entry itself is the section
 * @param newSectionAfter a new section starts after the entry
 * @param newSectionAfterLevel the level of the section that starts after the entry, or {@code null}
 * @param implicitSection a section opened by the entry before any other section is left out and the
 *     entry lies directly in the container
 * @param type the output type of the section type ({@code aggregator.type}), or {@code null}
 */
public record HierarchyRule(
    boolean newSection,
    @Nullable Integer sectionLevel,
    @Nullable String newSectionType,
    boolean newSectionAfter,
    @Nullable Integer newSectionAfterLevel,
    boolean implicitSection,
    @Nullable String type) {

  /** The type of a section if no other type is configured, as in the former aggregator. */
  public static final String DEFAULT_SECTION_TYPE = "section";

  /** The rule of an entry that only adds content. */
  public static final HierarchyRule CONTENT =
      new HierarchyRule(false, null, null, false, null, false);

  /** A rule without an output type of its own. */
  public HierarchyRule(
      boolean newSection,
      @Nullable Integer sectionLevel,
      @Nullable String newSectionType,
      boolean newSectionAfter,
      @Nullable Integer newSectionAfterLevel,
      boolean implicitSection) {
    this(
        newSection,
        sectionLevel,
        newSectionType,
        newSectionAfter,
        newSectionAfterLevel,
        implicitSection,
        null);
  }

  /**
   * Reads the rule from the {@code aggregator} configuration of a section type.
   *
   * @param aggregator the {@code aggregator} node of the section type's configuration
   * @return the rule
   */
  public static HierarchyRule of(Map<?, ?> aggregator) {
    return new HierarchyRule(
        Boolean.TRUE.equals(aggregator.get("newSection")),
        integer(aggregator.get("sectionLevel")),
        text(aggregator.get("newSectionType")),
        Boolean.TRUE.equals(aggregator.get("newSectionAfter")),
        integer(aggregator.get("newSectionAfterLevel")),
        Boolean.TRUE.equals(aggregator.get("implicitSection")),
        text(aggregator.get("type")));
  }

  /** Whether the entry itself becomes the opened section, rather than being wrapped by it. */
  public boolean itemIsSection() {
    return this.newSection && this.newSectionType == null;
  }

  /**
   * The type the former aggregator gives a section opened by the entry: {@code newSectionType},
   * else the output type of the entry's section type, else {@value #DEFAULT_SECTION_TYPE}.
   */
  public String outputType() {
    if (this.newSectionType != null) {
      return this.newSectionType;
    }
    return this.type == null ? DEFAULT_SECTION_TYPE : this.type;
  }

  private static @Nullable Integer integer(@Nullable Object value) {
    return value instanceof Number n ? n.intValue() : null;
  }

  private static @Nullable String text(@Nullable Object value) {
    return value instanceof String s && !s.isBlank() ? s : null;
  }
}
