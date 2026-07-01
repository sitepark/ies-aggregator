package com.sitepark.ies.aggregator.output.format;

import com.sitepark.ies.aggregator.output.DomainObjectMapper;
import com.sitepark.ies.aggregator.output.Output;
import com.sitepark.ies.aggregator.output.OutputList;
import com.sitepark.ies.aggregator.output.OutputListItem;
import com.sitepark.ies.aggregator.output.OutputNode;
import com.sitepark.ies.aggregator.output.OutputObject;
import com.sitepark.ies.aggregator.output.OutputVisitor;
import com.sitepark.ies.aggregator.value.text.Translations;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

/**
 * Visitor that serializes an {@link Output} tree to PHP array syntax and writes it to a {@link
 * Writer}.
 *
 * <p>Output rules:
 *
 * <ul>
 *   <li>{@link OutputObject} and {@link OutputListItem} become associative PHP arrays.
 *   <li>{@link OutputList}, raw {@link Collection}, and {@code Object[]} become indexed PHP arrays.
 *       Single-item lists are written on one line without newlines or indentation.
 *   <li>{@link Map} is written as an associative PHP array.
 *   <li>{@link RawPhpCode} is written verbatim, without quoting.
 *   <li>Strings are wrapped in double quotes with escape sequences ({@code \\}, {@code \"}, {@code
 *       \b}, {@code \t}, {@code \n}, {@code \f}, {@code \r}, {@code \$}); remaining control
 *       characters below {@code 0x20} are escaped as {@code \xXX}.
 *   <li>{@link Number} and {@link Boolean} are written unquoted.
 *   <li>{@code null} is written as {@code null}.
 * </ul>
 *
 * <p>Any {@link IOException} from the underlying writer is rethrown as {@link
 * UncheckedIOException}, since the visitor API does not declare checked exceptions.
 */
public final class PhpArrayWriter extends OutputVisitor {

  private static final String ARROW = " => ";
  private static final char INDENT_CHAR = '\t';

  private final Writer writer;
  private int indent;

  /**
   * Creates a writer that uses no domain object mapper and renders the source language.
   *
   * @param writer the target writer
   */
  public PhpArrayWriter(Writer writer) {
    this(writer, DomainObjectMapper.NONE, Translations.SOURCE);
  }

  /**
   * Creates a writer that uses no domain object mapper and applies the given translations.
   *
   * @param writer the target writer
   * @param translations the translation table (use {@link Translations#SOURCE} for the source
   *     language)
   */
  public PhpArrayWriter(Writer writer, Translations translations) {
    this(writer, DomainObjectMapper.NONE, translations);
  }

  /**
   * Creates a writer with a custom domain object mapper, rendering the source language.
   *
   * @param writer the target writer
   * @param domainObjectMapper the mapper for unwrapping domain objects
   */
  public PhpArrayWriter(Writer writer, DomainObjectMapper domainObjectMapper) {
    this(writer, domainObjectMapper, Translations.SOURCE);
  }

  /**
   * Creates a writer with a custom domain object mapper and translation table.
   *
   * @param writer the target writer
   * @param domainObjectMapper the mapper for unwrapping domain objects
   * @param translations the translation table (use {@link Translations#SOURCE} for the source
   *     language)
   */
  public PhpArrayWriter(
      Writer writer, DomainObjectMapper domainObjectMapper, Translations translations) {
    super(domainObjectMapper, translations);
    this.writer = writer;
  }

  @Override
  public void visitObject(OutputObject obj) {
    writeAssociative(obj);
  }

  @Override
  public void visitListItem(OutputListItem item) {
    writeAssociative(item);
  }

  @Override
  public void visitList(OutputList list) {
    writeIndexed(list.items().size(), list.items()::forEach, this::visitListItem);
  }

  @Override
  public void visitString(String value) {
    write(quote(value));
  }

  @Override
  public void visitNumber(Number value) {
    write(String.valueOf(value));
  }

  @Override
  public void visitBoolean(Boolean value) {
    write(value ? "true" : "false");
  }

  @Override
  public void visitNull() {
    write("null");
  }

  @Override
  public void visitRawPhpCode(RawPhpCode value) {
    write(value.code());
  }

  @Override
  public void visitMap(Map<?, ?> map) {
    if (map.isEmpty()) {
      write("[]");
      return;
    }
    write("[");
    this.indent++;
    boolean first = true;
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      if (!first) {
        write(",");
      }
      write("\n");
      writeIndentation();
      String key = entry.getKey() == null ? "" : entry.getKey().toString();
      writeKey(key);
      visitField(key, entry.getValue());
      first = false;
    }
    this.indent--;
    write("\n");
    writeIndentation();
    write("]");
  }

  @Override
  public void visitCollection(Collection<?> collection) {
    Object[] items = collection.toArray();
    writeIndexed(
        items.length,
        c -> {
          for (Object item : items) {
            c.accept(item);
          }
        },
        item -> visitField(null, item));
  }

  @Override
  public void visitArray(Object[] array) {
    writeIndexed(
        array.length,
        c -> {
          for (Object item : array) {
            c.accept(item);
          }
        },
        item -> visitField(null, item));
  }

  @Override
  public void visitUnknown(Object value) {
    write(quote(value == null ? "" : value.toString()));
  }

  private void writeAssociative(OutputNode node) {
    Map<String, Object> entries = node.entries();
    if (entries.isEmpty()) {
      write("[]");
      return;
    }
    write("[");
    this.indent++;
    boolean first = true;
    for (Map.Entry<String, Object> entry : entries.entrySet()) {
      if (!first) {
        write(",");
      }
      write("\n");
      writeIndentation();
      writeKey(entry.getKey());
      visitField(entry.getKey(), entry.getValue());
      first = false;
    }
    this.indent--;
    write("\n");
    writeIndentation();
    write("]");
  }

  @FunctionalInterface
  private interface ItemIterator<T> {
    void forEach(java.util.function.Consumer<T> consumer);
  }

  private <T> void writeIndexed(
      int size, ItemIterator<T> iterator, java.util.function.Consumer<T> writeItem) {
    if (size == 0) {
      write("[]");
      return;
    }
    write("[");
    if (size == 1) {
      iterator.forEach(writeItem);
    } else {
      this.indent++;
      boolean[] first = {true};
      iterator.forEach(
          item -> {
            if (!first[0]) {
              write(",");
            }
            write("\n");
            writeIndentation();
            writeItem.accept(item);
            first[0] = false;
          });
      this.indent--;
      write("\n");
      writeIndentation();
    }
    write("]");
  }

  private void writeKey(String key) {
    write(quote(key));
    write(ARROW);
  }

  private void writeIndentation() {
    for (int i = 0; i < this.indent; i++) {
      write(INDENT_CHAR);
    }
  }

  private void write(String s) {
    try {
      this.writer.write(s);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void write(char c) {
    try {
      this.writer.write(c);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  static String quote(String string) {
    if (string == null || string.isEmpty()) {
      return "\"\"";
    }
    StringBuilder sb = new StringBuilder(string.length() + 4);
    sb.append('"');
    for (int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);
      switch (c) {
        case '\\' -> sb.append("\\\\");
        case '"' -> sb.append("\\\"");
        case '\b' -> sb.append("\\b");
        case '\t' -> sb.append("\\t");
        case '\n' -> sb.append("\\n");
        case '\f' -> sb.append("\\f");
        case '\r' -> sb.append("\\r");
        case '$' -> sb.append("\\$");
        default -> {
          // Remaining control characters below 0x20 are emitted as \xXX hex escapes (always two
          // digits, so a following hex character cannot extend the escape) instead of raw bytes,
          // which would flag the .php file as binary and render invisibly.
          if (c < 0x20) {
            sb.append(String.format("\\x%02x", (int) c));
          } else {
            sb.append(c);
          }
        }
      }
    }
    sb.append('"');
    return sb.toString();
  }
}
