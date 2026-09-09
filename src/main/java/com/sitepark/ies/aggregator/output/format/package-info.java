/**
 * Turning an output tree into a document — the two layers that takes.
 *
 * <p>The <b>value layer</b> renders one value or subtree into a literal. Its members are the {@link
 * com.sitepark.ies.aggregator.output.OutputVisitor OutputVisitor} subclasses of this package —
 * {@link com.sitepark.ies.aggregator.output.format.PhpArrayWriter PhpArrayWriter}, {@link
 * com.sitepark.ies.aggregator.output.format.JsonWriter JsonWriter} — together with the value types
 * only a format knows, such as {@link com.sitepark.ies.aggregator.output.format.Code Code} for
 * content that must not be quoted.
 *
 * <p>The <b>document layer</b> is {@link
 * com.sitepark.ies.aggregator.output.format.OutputWriter OutputWriter}: it writes the complete file
 * a channel publishes, frame included, and decides which subtrees go into it. It is an interface
 * only. A frame says how a consumer loads the file, and that belongs to the product that owns the
 * consumer — so no implementation of it lives here, not even for a format that looks neutral.
 *
 * <p>The two layers compose rather than inherit: a document writer hands a subtree to a value writer
 * and keeps the frame to itself. That the value writers are {@code final} is therefore no
 * restriction but the intended direction of use, and it has a second benefit — a fresh instance per
 * subtree starts with a fresh indentation.
 */
@NullMarked
package com.sitepark.ies.aggregator.output.format;

import org.jspecify.annotations.NullMarked;
