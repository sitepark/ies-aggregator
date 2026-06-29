package com.sitepark.ies.aggregator.output;

import java.util.Objects;

/**
 * A single entry inside an {@link OutputList}, itself an {@link OutputNode} that stores named
 * fields.
 *
 * <p>{@link #field()} and {@link #path()} include the zero-based index position within the parent
 * list, e.g. {@code "items[2]"}.
 */
public class OutputListItem extends OutputNode {

  /**
   * @param parent the list that contains this item; must not be {@code null}
   */
  public OutputListItem(OutputList parent) {
    super(parent);
  }

  @Override
  public OutputList parent() {
    return (OutputList) Objects.requireNonNull(super.parent());
  }

  @Override
  public OutputObject root() {
    return this.parent().root();
  }

  @Override
  public String field() {
    return this.parent().field() + "[" + this.parent().indexOf(this) + "]";
  }

  @Override
  public String path() {
    return this.parent().path() + "[" + this.parent().indexOf(this) + "]";
  }

  @Override
  public void accept(OutputVisitor visitor) {
    visitor.visitListItem(this);
  }
}
