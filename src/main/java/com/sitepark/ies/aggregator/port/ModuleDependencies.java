package com.sitepark.ies.aggregator.port;

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
 * <p>The version is not an argument: the implementation reads it from the modules installed in the
 * environment at the moment of the call, so the dependency always names the version the output was
 * written against. A module id the environment does not know is dropped rather than failing the
 * aggregation.
 */
public interface ModuleDependencies {

  /**
   * Reports that the resource depends on the installed version of a module.
   *
   * @param moduleId the id of the module, as the module map of the environment knows it
   */
  void dependsOn(String moduleId);
}
