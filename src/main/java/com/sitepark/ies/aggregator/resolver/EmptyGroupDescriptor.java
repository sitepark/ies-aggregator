package com.sitepark.ies.aggregator.resolver;

/**
 * A {@link GroupDescriptor} that carries no data — the null object returned by {@link
 * GroupDescriptor#empty()}.
 *
 * <p>Stateless, therefore a single shared {@link #INSTANCE}.
 */
final class EmptyGroupDescriptor implements GroupDescriptor {

  static final EmptyGroupDescriptor INSTANCE = new EmptyGroupDescriptor();

  private EmptyGroupDescriptor() {}

  /**
   * Returns {@code 0}; an empty group has no id.
   *
   * @return {@code 0}
   */
  @Override
  public int id() {
    return 0;
  }

  /**
   * Returns the empty string; an empty group has no qualified id.
   *
   * @return the empty string
   */
  @Override
  public String qualifiedId() {
    return "";
  }

  /**
   * Returns the empty string; an empty group has no version.
   *
   * @return the empty string
   */
  @Override
  public String version() {
    return "";
  }

  /**
   * Returns the empty string; an empty group has no type.
   *
   * @return the empty string
   */
  @Override
  public String type() {
    return "";
  }

  /**
   * Returns the empty string; an empty group has no name.
   *
   * @return the empty string
   */
  @Override
  public String name() {
    return "";
  }

  /**
   * Returns the empty string; a group is not published as a file.
   *
   * @return the empty string
   */
  @Override
  public String filename() {
    return "";
  }

  /**
   * Returns the empty string; an empty group has no anchor.
   *
   * @return the empty string
   */
  @Override
  public String anchor() {
    return "";
  }

  /**
   * Returns the empty revision; an empty group was never created.
   *
   * @return {@link Revision#empty()}
   */
  @Override
  public Revision created() {
    return Revision.empty();
  }

  /**
   * Returns the empty revision; an empty group was never changed.
   *
   * @return {@link Revision#empty()}
   */
  @Override
  public Revision changed() {
    return Revision.empty();
  }

  /**
   * Returns {@code false}; an empty group is not a site root.
   *
   * @return {@code false}
   */
  @Override
  public boolean isRootSite() {
    return false;
  }

  /**
   * Returns {@code false}; an empty group is not a microsite root.
   *
   * @return {@code false}
   */
  @Override
  public boolean isMicrositeRootSite() {
    return false;
  }

  /**
   * Returns the empty string; an empty group has no language.
   *
   * @return the empty string
   */
  @Override
  public String lang() {
    return "";
  }
}
