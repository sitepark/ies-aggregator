package com.sitepark.ies.aggregator.resolver;

/**
 * An {@link EntityDescriptor} that carries no data — the null object returned by {@link
 * EntityDescriptor#empty()}.
 *
 * <p>Stateless, therefore a single shared {@link #INSTANCE}.
 */
final class EmptyEntityDescriptor implements EntityDescriptor {

  static final EmptyEntityDescriptor INSTANCE = new EmptyEntityDescriptor();

  private EmptyEntityDescriptor() {}

  /**
   * Returns {@code 0}; an empty entity has no id.
   *
   * @return {@code 0}
   */
  @Override
  public int id() {
    return 0;
  }

  /**
   * Returns the empty string; an empty entity has no qualified id.
   *
   * @return the empty string
   */
  @Override
  public String qualifiedId() {
    return "";
  }

  /**
   * Returns the empty string; an empty entity has no version.
   *
   * @return the empty string
   */
  @Override
  public String version() {
    return "";
  }

  /**
   * Returns the empty string; an empty entity has no type.
   *
   * @return the empty string
   */
  @Override
  public String type() {
    return "";
  }

  /**
   * Returns the empty string; an empty entity has no file name.
   *
   * @return the empty string
   */
  @Override
  public String filename() {
    return "";
  }

  /**
   * Returns the empty string; an empty entity has no name.
   *
   * @return the empty string
   */
  @Override
  public String name() {
    return "";
  }

  /**
   * Returns the empty string; an empty entity has no anchor.
   *
   * @return the empty string
   */
  @Override
  public String anchor() {
    return "";
  }

  /**
   * Returns the empty revision; an empty entity was never created.
   *
   * @return {@link Revision#empty()}
   */
  @Override
  public Revision created() {
    return Revision.empty();
  }

  /**
   * Returns the empty revision; an empty entity was never changed.
   *
   * @return {@link Revision#empty()}
   */
  @Override
  public Revision changed() {
    return Revision.empty();
  }
}
