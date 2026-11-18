package com.sitepark.ies.aggregator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * The part of a section type's configuration a container needs to place an entry of that type, as
 * {@code sectionType.spml} reads it for every entry.
 *
 * @param id the id the configuration names the section type by; never empty
 * @param enabled {@code false} if the configuration switches the type off ({@code enabled: false})
 * @param natures for each channel nature the configuration names under {@code aggregator.natures},
 *     whether the type is enabled there; {@code null} if it names none
 * @param type the output type of the component ({@code aggregator.type}), or {@code null}
 * @param componentId the fixed id of the component ({@code aggregator.id}), or {@code null}
 * @param config the {@code aggregator.config} the component carries, or {@code null}
 * @param subinformationFieldName the field the entry's scope moves into ({@code
 *     template.subinformationFieldName}), or {@code null}
 */
public record SubSectionType(
    String id,
    boolean enabled,
    @Nullable Map<String, Boolean> natures,
    @Nullable String type,
    @Nullable String componentId,
    @Nullable Object config,
    @Nullable String subinformationFieldName) {

  public SubSectionType {
    natures = natures == null ? null : Map.copyOf(natures);
  }

  /**
   * Reads the section type from its configuration.
   *
   * @param id the id the configuration names the section type by
   * @param enabled the value of the section type's {@code enabled}, or {@code null} if it has none
   * @param aggregator the {@code aggregator} node of the section type's configuration
   * @param template the {@code template} node of the section type's configuration
   * @return the section type
   */
  public static SubSectionType of(
      String id, @Nullable Object enabled, Map<?, ?> aggregator, Map<?, ?> template) {
    return new SubSectionType(
        id,
        // only an explicit false switches a type off, as `${_sectionType.enabled} == false`
        !Boolean.FALSE.equals(enabled) && !"false".equals(enabled),
        natures(aggregator.get("natures")),
        text(aggregator.get("type")),
        text(aggregator.get("id")),
        aggregator.get("config"),
        text(template.get("subinformationFieldName")));
  }

  /**
   * Whether an entry of this type is placed in a channel of the given nature, as {@code
   * sectionType.spml} decides it: not if the type is switched off; and if both the channel declares
   * a nature and the type names natures, only if the type is enabled for that very nature.
   *
   * @param nature the nature of the channel, or empty if it declares none
   * @return whether the entry is placed
   */
  public boolean enabledFor(Optional<String> nature) {
    if (!this.enabled) {
      return false;
    }
    if (nature.isEmpty() || this.natures == null) {
      return true;
    }
    return Boolean.TRUE.equals(this.natures.get(nature.get()));
  }

  /** The type of the component an entry of this type gets: {@code aggregator.type}, else the id. */
  public String componentType() {
    return this.type != null ? this.type : this.id;
  }

  // null is not "no natures": natures {} switches the type off in every channel that declares a
  // nature, while no natures at all leave it on - the former aggregator tells them apart by isNull
  @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
  private static @Nullable Map<String, Boolean> natures(@Nullable Object natures) {
    if (!(natures instanceof Map<?, ?> map)) {
      return null;
    }
    Map<String, Boolean> enabledByNature = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      // `natures.<nature>.enabled != true` switches the type off: anything but true is off
      boolean enabled =
          entry.getValue() instanceof Map<?, ?> nature
              && (Boolean.TRUE.equals(nature.get("enabled"))
                  || "true".equals(nature.get("enabled")));
      enabledByNature.put(String.valueOf(entry.getKey()), enabled);
    }
    return enabledByNature;
  }

  private static @Nullable String text(@Nullable Object value) {
    return value instanceof String s && !s.isEmpty() ? s : null;
  }
}
