package com.sitepark.ies.aggregator.output.format;

import com.sitepark.ies.aggregator.output.OutputObject;
import com.sitepark.ies.aggregator.output.OutputVisitor;
import java.io.IOException;
import java.io.Writer;

/**
 * Writes a finished output tree as the complete document a channel publishes — frame included.
 *
 * <p>The frame is part of the format, not decoration around it. A file that must {@code return} an
 * executable object, a file that must return plain data, a bare JSON document and a raw text file
 * differ in how their consumer loads them, and that difference belongs to the product that owns the
 * consumer. This package therefore contributes the interface and not a single implementation of it:
 * even a seemingly neutral format carries product decisions, down to the indentation.
 *
 * <h2>Two layers</h2>
 *
 * <p>An implementation <b>composes</b> rather than inherits. {@link OutputVisitor} and its
 * subclasses are the <i>value layer</i> — they turn one value or subtree into a literal. An {@code
 * OutputWriter} is the <i>document layer</i>: it decides which subtrees are written, in what order,
 * and what goes between them. A writer that needs a value rendered hands the subtree to a visitor
 * and keeps the frame to itself.
 *
 * <p>The value-layer writers of this package are {@code final} on purpose. Composing them is not a
 * workaround but the intended use, and it has a second benefit: a fresh instance per subtree starts
 * with a fresh indentation, which is what a document with several independently indented blocks
 * needs.
 */
public interface OutputWriter {

  /**
   * Writes the document for the given tree.
   *
   * @param root the aggregated tree, as returned by the root aggregator
   * @param context what the file being written needs beside the tree
   * @param target the writer the document goes to; not closed by this method
   * @throws IOException if writing fails
   */
  void write(OutputObject root, OutputContext context, Writer target) throws IOException;
}
