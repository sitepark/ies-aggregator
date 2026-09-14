package com.sitepark.ies.aggregator.output.format;

import com.sitepark.ies.aggregator.output.DomainObjectMapper;
import com.sitepark.ies.aggregator.output.EmptyValuePolicy;
import com.sitepark.ies.aggregator.output.Output;
import com.sitepark.ies.aggregator.output.OutputList;
import com.sitepark.ies.aggregator.output.OutputListItem;
import com.sitepark.ies.aggregator.output.OutputNode;
import com.sitepark.ies.aggregator.output.OutputObject;
import com.sitepark.ies.aggregator.output.OutputVisitor;
import com.sitepark.ies.aggregator.value.text.PlainText;
import com.sitepark.ies.aggregator.value.text.TranslatableSplitText;
import com.sitepark.ies.aggregator.value.text.TranslatableText;
import com.sitepark.ies.aggregator.value.text.Translations;
import com.sitepark.ies.aggregator.value.uri.PlainUri;
import com.sitepark.ies.aggregator.value.uri.TranslatableUri;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Visitor that serializes an {@link Output} tree to JSON and writes it to a {@link Writer}.
 *
 * <p>Output rules:
 *
 * <ul>
 *   <li>{@link OutputObject} and {@link OutputListItem} become JSON objects.
 *   <li>{@link OutputList}, raw {@link Collection}, and {@code Object[]} become JSON arrays.
 *   <li>{@link Map} becomes a JSON object. Keys are converted to strings via {@code toString()}.
 *   <li>{@link PlainText}, {@link TranslatableText}, {@link PlainUri}, {@link TranslatableUri},
 *       {@link TranslatableSplitText}, and both kinds of {@link Code} ({@link RawPhpCode}, {@link
 *       PlainCode}) are written as their {@code toString()} representation (a quoted JSON string).
 *   <li>{@link Number} and {@link Boolean} are written unquoted.
 *   <li>{@link java.time.Instant} is written as a quoted ISO-8601 string (unlike the {@link
 *       PhpArrayWriter}, which emits epoch seconds).
 *   <li>{@code null} is written as {@code null}.
 * </ul>
 *
 * <p>Indentation is given by a {@link JsonIndent} and defaults to {@link JsonIndent#NONE} — compact
 * output on a single line. With an indenting one, every entry goes on its own line, a space follows
 * each key, and an empty object or array still stays on one line as {@code {}} or {@code []}.
 *
 * <p>Any {@link IOException} from the underlying writer is rethrown as {@link
 * UncheckedIOException}.
 */
public final class JsonWriter extends OutputVisitor {

  private final Writer writer;

  private final JsonIndent indentation;

  private int indentWidth;

  /**
   * Creates a writer that uses no domain object mapper and renders the source language.
   *
   * @param writer the target writer
   */
  public JsonWriter(Writer writer) {
    this(writer, DomainObjectMapper.NONE, Translations.SOURCE);
  }

  /**
   * Creates a writer that uses no domain object mapper and applies the given translations.
   *
   * @param writer the target writer
   * @param translations the translation table (use {@link Translations#SOURCE} for the source
   *     language)
   */
  public JsonWriter(Writer writer, Translations translations) {
    this(writer, DomainObjectMapper.NONE, translations);
  }

  /**
   * Creates a writer with a custom domain object mapper, rendering the source language.
   *
   * @param writer the target writer
   * @param domainObjectMapper the mapper for unwrapping domain objects
   */
  public JsonWriter(Writer writer, DomainObjectMapper domainObjectMapper) {
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
  public JsonWriter(
      Writer writer, DomainObjectMapper domainObjectMapper, Translations translations) {
    this(writer, domainObjectMapper, translations, EmptyValuePolicy.ANNOTATED);
  }

  /**
   * Creates a writer with a custom domain object mapper, translation table and empty-value policy.
   *
   * @param writer the target writer
   * @param domainObjectMapper the mapper for unwrapping domain objects
   * @param translations the translation table (use {@link Translations#SOURCE} for the source
   *     language)
   * @param emptyValuePolicy the policy deciding which empty values are rendered anyway (use {@link
   *     EmptyValuePolicy#ANNOTATED} for the default behavior)
   */
  public JsonWriter(
      Writer writer,
      DomainObjectMapper domainObjectMapper,
      Translations translations,
      EmptyValuePolicy emptyValuePolicy) {
    this(writer, domainObjectMapper, translations, emptyValuePolicy, JsonIndent.NONE);
  }

  /**
   * Creates a writer with a custom domain object mapper, translation table, empty-value policy and
   * indentation.
   *
   * @param writer the target writer
   * @param domainObjectMapper the mapper for unwrapping domain objects
   * @param translations the translation table (use {@link Translations#SOURCE} for the source
   *     language)
   * @param emptyValuePolicy the policy deciding which empty values are rendered anyway (use {@link
   *     EmptyValuePolicy#ANNOTATED} for the default behavior)
   * @param indentation how the document is indented (use {@link JsonIndent#NONE} for compact
   *     output)
   */
  public JsonWriter(
      Writer writer,
      DomainObjectMapper domainObjectMapper,
      Translations translations,
      EmptyValuePolicy emptyValuePolicy,
      JsonIndent indentation) {
    super(domainObjectMapper, translations, emptyValuePolicy);
    this.writer = writer;
    this.indentation = indentation;
    this.indentWidth = indentation.initial();
  }

  @Override
  public void visitObject(OutputObject obj) {
    writeJsonObject(obj);
  }

  @Override
  public void visitListItem(OutputListItem item) {
    writeJsonObject(item);
  }

  @Override
  public void visitList(OutputList list) {
    writeJsonArray(nonEmptyItems(list), this::visitListItem);
  }

  @Override
  public void visitString(String value) {
    writeQuoted(value);
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

  // PlainText, TranslatableText, PlainUri, TranslatableUri, TranslatableSplitText and Code use the
  // OutputVisitor defaults, which render via the translation table and delegate to visitString() —
  // i.e. a quoted JSON string. Raw PHP code has no meaning in JSON and is quoted like any other
  // string.

  @Override
  public void visitMap(Map<?, ?> map) {
    Map<?, ?> entries = nonEmptyMap(map);
    if (entries.isEmpty()) {
      write("{}");
      return;
    }
    openBlock('{');
    boolean first = true;
    for (Map.Entry<?, ?> entry : entries.entrySet()) {
      if (!first) {
        write(',');
      }
      newLine();
      String key = entry.getKey() == null ? "" : entry.getKey().toString();
      writeKey(key);
      visitField(key, entry.getValue());
      first = false;
    }
    closeBlock('}');
  }

  @Override
  public void visitCollection(Collection<?> collection) {
    writeJsonArray(nonEmptyElements(collection), item -> visitField(null, item));
  }

  @Override
  public void visitArray(Object[] array) {
    writeJsonArray(nonEmptyElements(List.of(array)), item -> visitField(null, item));
  }

  @Override
  public void visitUnknown(Object value) {
    writeQuoted(value == null ? "" : value.toString());
  }

  private void writeJsonObject(OutputNode node) {
    Map<String, Object> entries = nonEmptyEntries(node);
    if (entries.isEmpty()) {
      write("{}");
      return;
    }
    openBlock('{');
    boolean first = true;
    for (Map.Entry<String, Object> entry : entries.entrySet()) {
      if (!first) {
        write(',');
      }
      newLine();
      writeKey(entry.getKey());
      visitField(entry.getKey(), entry.getValue());
      first = false;
    }
    closeBlock('}');
  }

  private <T> void writeJsonArray(List<T> items, Consumer<T> writeItem) {
    if (items.isEmpty()) {
      write("[]");
      return;
    }
    openBlock('[');
    boolean first = true;
    for (T item : items) {
      if (!first) {
        write(',');
      }
      newLine();
      writeItem.accept(item);
      first = false;
    }
    closeBlock(']');
  }

  private void writeKey(String key) {
    writeQuoted(key);
    write(this.indentation.isCompact() ? ":" : ": ");
  }

  private void openBlock(char bracket) {
    write(bracket);
    this.indentWidth += this.indentation.factor();
  }

  private void closeBlock(char bracket) {
    this.indentWidth -= this.indentation.factor();
    newLine();
    write(bracket);
  }

  /**
   * Starts the next line of the document, indented to the current level. Writes nothing at all when
   * the output is compact.
   */
  private void newLine() {
    if (this.indentation.isCompact()) {
      return;
    }
    write('\n');
    for (int i = 0; i < this.indentWidth; i++) {
      write(this.indentation.indentChar());
    }
  }

  private void writeQuoted(String s) {
    if (s == null) {
      write("null");
      return;
    }
    StringBuilder sb = new StringBuilder(s.length() + 4);
    sb.append('"');
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\\' -> sb.append("\\\\");
        case '"' -> sb.append("\\\"");
        case '\b' -> sb.append("\\b");
        case '\f' -> sb.append("\\f");
        case '\n' -> sb.append("\\n");
        case '\r' -> sb.append("\\r");
        case '\t' -> sb.append("\\t");
        default -> {
          // Control chars, plus U+2028/U+2029 which are valid in JSON but break JavaScript when
          // the output is eval'd (e.g. JSONP), are emitted as \\uXXXX escapes.
          if (c < 0x20 || c == 0x2028 || c == 0x2029) {
            sb.append(String.format("\\u%04x", (int) c));
          } else {
            sb.append(c);
          }
        }
      }
    }
    sb.append('"');
    write(sb.toString());
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
}
