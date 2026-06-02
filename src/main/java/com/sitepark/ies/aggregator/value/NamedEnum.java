package com.sitepark.ies.aggregator.value;

/**
 * Marker interface for enums that expose a stable, external {@code name} which differs from {@link
 * Enum#name()} and can be used for serialization, deserialization and lookup purposes.
 *
 * <p>{@link Enum#name()} returns the Java identifier of the enum constant (e.g. {@code
 * INTERNAL_WITH_PARAMETERS}). This identifier is bound to the source code and not suitable as a
 * stable representation in APIs, persisted data or configuration files. Implementations of this
 * interface provide an additional, framework-independent name (e.g. {@code
 * "internalWithParameters"}) that remains stable even if the Java constant is renamed.
 *
 * <p>Typical use cases are:
 *
 * <ul>
 *   <li>Serialization and deserialization of enum values in JSON, XML or other text-based formats
 *       without depending on a specific framework.
 *   <li>Persistence of enum values in databases or configuration files using a human-readable
 *       representation.
 *   <li>Generic resolution of enum constants by name via {@link ResolvedValue#asEnum(Class)}.
 * </ul>
 *
 * <h2>Contract</h2>
 *
 * <ul>
 *   <li>{@link #getName()} must never return {@code null}.
 *   <li>The returned name must be unique within the implementing enum type.
 *   <li>The returned name should be stable across releases; changing it is considered a breaking
 *       change for any consumer relying on it.
 * </ul>
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * public enum LinkType implements NamedEnum {
 *   INTERNAL("internal"),
 *   INTERNAL_WITH_PARAMETERS("internalWithParameters"),
 *   EXTERNAL("external"),
 *   DOWNLOAD("download");
 *
 *   private final String name;
 *
 *   LinkType(String name) {
 *     this.name = name;
 *   }
 *
 *   @Override
 *   public String getName() {
 *     return name;
 *   }
 * }
 *
 * LinkType type = resolver.value("type").asEnum(LinkType.class);
 * }</pre>
 */
public interface NamedEnum {
  /**
   * Returns the stable, external name of this enum constant.
   *
   * <p>Unlike {@link Enum#name()}, the returned value is intended to be used in external
   * representations such as JSON payloads, persisted data or configuration files. It must be unique
   * within the implementing enum type and must not be {@code null}.
   *
   * @return the external name of this enum constant; never {@code null}
   */
  String getName();
}
