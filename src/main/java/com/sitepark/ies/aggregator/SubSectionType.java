package com.sitepark.ies.aggregator;

import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * The part of a section type's configuration a container needs to place an entry of that type, as
 * {@code sectionType.spml} reads it for every entry.
 *
 * <p>Whether the type is placed at all - switched off by {@code enabled: false}, or not enabled for
 * the nature of the channel by {@code aggregator.natures} - is not part of it: the platform decides
 * that and does not hand such a type in. {@code aggregator.config}, which {@code sectionType.spml}
 * copies onto the component, is not carried either: no project configures it, and its purpose is
 * open even in SiteKit.
 *
 * @param id the id the configuration names the section type by; never empty
 * @param type the output type of the component ({@code aggregator.type}), or {@code null}
 * @param componentId the fixed id of the component ({@code aggregator.id}), or {@code null}
 * @param subinformationFieldName the field the entry's scope moves into ({@code
 *     template.subinformationFieldName}), or {@code null}
 * @param content the node of the current section an entry of this type is placed in ({@code
 *     aggregator.content}), or {@code null} to place it in the section itself
 */
public record SubSectionType(
    String id,
    @Nullable String type,
    @Nullable String componentId,
    @Nullable String subinformationFieldName,
    @Nullable Content content) {

  /**
   * The node of the current section an entry is placed in, as {@code sectionType.spml:50-93}
   * resolves {@code aggregator.content}: the node of that type (and id, if one is given) the section
   * holds already, else a new one with the given id or the next of its type, which joins the section
   * only if anything was placed in it.
   *
   * @param type the type of the node; never empty
   * @param id the id of the node, or {@code null} to look for any node of the type and to draw the
   *     id of a new one
   */
  public record Content(String type, @Nullable String id) {}

  /** A section type that is placed in the section itself. */
  public SubSectionType(
      String id,
      @Nullable String type,
      @Nullable String componentId,
      @Nullable String subinformationFieldName) {
    this(id, type, componentId, subinformationFieldName, null);
  }

  /**
   * Reads the section type from its configuration.
   *
   * @param id the id the configuration names the section type by
   * @param aggregator the {@code aggregator} node of the section type's configuration
   * @param template the {@code template} node of the section type's configuration
   * @return the section type
   */
  public static SubSectionType of(String id, Map<?, ?> aggregator, Map<?, ?> template) {
    return new SubSectionType(
        id,
        text(aggregator.get("type")),
        text(aggregator.get("id")),
        text(template.get("subinformationFieldName")),
        content(aggregator.get("content")));
  }

  private static @Nullable Content content(@Nullable Object content) {
    if (!(content instanceof Map<?, ?> node) || text(node.get("type")) == null) {
      return null;
    }
    return new Content(String.valueOf(node.get("type")), text(node.get("id")));
  }

  /** The type of the component an entry of this type gets: {@code aggregator.type}, else the id. */
  public String componentType() {
    return this.type != null ? this.type : this.id;
  }

  private static @Nullable String text(@Nullable Object value) {
    return value instanceof String s && !s.isEmpty() ? s : null;
  }
}
