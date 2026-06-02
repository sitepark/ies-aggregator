package com.sitepark.ies.aggregator.output;

/**
 * Concrete {@link OutputNode} representing a single object node in the output tree.
 *
 * <p>An {@code OutputObject} is identified by the field name under which it is stored in its
 * parent. The root object has a {@code null} field name and a {@code null} parent.
 */
public class OutputObject extends OutputNode {

  private final String field;

  /**
   * @param field the field name under which this object is stored; {@code null} for the root
   * @param parent the parent node; {@code null} for the root
   */
  public OutputObject(String field, OutputNode parent) {
    super(parent);
    this.field = field;
  }

  @Override
  public String field() {
    return this.field;
  }

  @Override
  public OutputObject root() {
    if (super.parent() == null) {
      return this;
    }
    return this.parent().root();
  }

  @Override
  public void accept(OutputVisitor visitor) {
    visitor.visitObject(this);
  }

  @Override
  public String path() {
    StringBuilder b = new StringBuilder();
    if (this.parent() != null) {
      b.append(this.parent().path());
    }
    if (this.field != null) {
      if (!b.isEmpty()) {
        b.append('.');
      }
      b.append(this.field);
    }
    return b.toString();
  }
}
