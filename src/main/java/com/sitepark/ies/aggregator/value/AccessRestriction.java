package com.sitepark.ies.aggregator.value;

import java.util.List;

/**
 * Who may see an object in a channel.
 *
 * <p>A restriction names a set of groups and whether membership grants or denies access. The
 * absence of a restriction is expressed by the absence of this value, not by an instance with an
 * empty group list — a consumer that receives one always has a rule to apply.
 *
 * @param mode whether the named groups are the ones allowed in, or the ones kept out
 * @param groups the ids of the groups the mode applies to
 */
public record AccessRestriction(Mode mode, List<String> groups) {

  public AccessRestriction {
    groups = List.copyOf(groups);
  }

  /** How the named groups are to be read. */
  public enum Mode {
    /** Only members of the named groups may see the object. */
    ALLOW,
    /** Members of the named groups may not see the object; everyone else may. */
    DENY
  }
}
