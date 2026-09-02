package com.sitepark.ies.aggregator.resolver;

/**
 * An {@link User} that carries no data — the null object returned by {@link User#empty()}.
 *
 * <p>Stateless, therefore a single shared {@link #INSTANCE}.
 */
final class EmptyUser implements User {

  static final EmptyUser INSTANCE = new EmptyUser();

  private EmptyUser() {}

  /**
   * Returns the empty string; an empty user has no id.
   *
   * @return the empty string
   */
  @Override
  public String id() {
    return "";
  }

  /**
   * Returns the empty string; an empty user has no anchor.
   *
   * @return the empty string
   */
  @Override
  public String anchor() {
    return "";
  }

  /**
   * Returns the empty string; an empty user has no name.
   *
   * @return the empty string
   */
  @Override
  public String name() {
    return "";
  }

  /**
   * Returns the empty string; an empty user has no given name.
   *
   * @return the empty string
   */
  @Override
  public String firstName() {
    return "";
  }

  /**
   * Returns the empty string; an empty user has no family name.
   *
   * @return the empty string
   */
  @Override
  public String lastName() {
    return "";
  }
}
