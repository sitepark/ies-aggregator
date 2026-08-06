package com.sitepark.ies.aggregator.resolver;

/**
 * An {@link Editor} that carries no data — the null object returned by {@link Editor#empty()}.
 *
 * <p>Stateless, therefore a single shared {@link #INSTANCE}.
 */
final class EmptyEditor implements Editor {

  static final EmptyEditor INSTANCE = new EmptyEditor();

  private EmptyEditor() {}

  /**
   * Returns the empty string; an empty editor has no id.
   *
   * @return the empty string
   */
  @Override
  public String id() {
    return "";
  }

  /**
   * Returns the empty string; an empty editor has no name.
   *
   * @return the empty string
   */
  @Override
  public String name() {
    return "";
  }
}
