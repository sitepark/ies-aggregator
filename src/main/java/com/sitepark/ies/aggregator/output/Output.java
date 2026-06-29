package com.sitepark.ies.aggregator.output;

import org.jspecify.annotations.Nullable;

/**
 * Common interface for all nodes in an output tree ({@link OutputObject}, {@link OutputList},
 * {@link OutputListItem}).
 *
 * <p>Every node knows its parent and can walk up to the root. The Visitor pattern is supported
 * via {@link #accept(OutputVisitor)}.
 */
public interface Output {

  /** Returns the parent node, or {@code null} if this is the root. */
  @Nullable Output parent();

  /** Returns {@code true} if this node has no parent. */
  default boolean isRoot() {
    return parent() == null;
  }

  /** Returns the root {@link OutputObject} of the tree. */
  OutputObject root();

  /** Returns the field name under which this node is stored in its parent. */
  @Nullable String field();

  /** Returns the dot-separated path from the root to this node (e.g. {@code "a.b.c[0]"}). */
  String path();

  /**
   * Accepts the visitor, dispatching to the correct {@code visit*} method.
   *
   * @param visitor the visitor to accept
   */
  void accept(OutputVisitor visitor);
}
