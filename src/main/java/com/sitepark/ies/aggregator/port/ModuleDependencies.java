package com.sitepark.ies.aggregator.port;

import java.util.Optional;

/**
 * Tells the publisher which installed modules the resource being aggregated depends on.
 *
 * <p>A side channel, not part of the output: the publisher keeps the module and its version per
 * published object, and generates the resource again when that module is updated. Nothing reported
 * here shows up in the resource itself. An aggregator reports a module when its output holds
 * something that only the installed version of the module makes true - a path into the module's
 * deployed CSS or JavaScript, say - and would be wrong after an update. Like {@link PublishedLinks}
 * and {@link Regeneration} it only adds information; reporting the same module twice is harmless.
 *
 * <p>The version is not an argument of {@link #dependsOn}: the implementation reads it from the
 * modules installed in the environment at the moment of the call, so the dependency always names
 * the version the output was written against. An aggregator that prints the version itself asks for
 * it through {@link #version}, which reports the dependency as well. A module id the environment
 * does not know is dropped rather than failing the aggregation.
 */
public interface ModuleDependencies {

  /**
   * Reports that the resource depends on the installed version of a module.
   *
   * @param moduleId the id of the module, as the module map of the environment knows it
   */
  void dependsOn(String moduleId);

  /**
   * Reports that the resource depends on the installed version of a module, like {@link
   * #dependsOn}, and returns that version.
   *
   * <p>Reading the version is the dependency: an output that holds the version of a module - a path
   * into the module's deployed files, say - is wrong after an update, so no caller can print the
   * version and forget to report it. The version is the string the module declares ({@code
   * system.module.version} in the legacy templates), and it is the very one the reported dependency
   * names.
   *
   * @param moduleId the id of the module, as the module map of the environment knows it
   * @return the installed version, or empty if the environment does not know the module; nothing is
   *     reported then, as for {@link #dependsOn}
   */
  Optional<String> version(String moduleId);
}
