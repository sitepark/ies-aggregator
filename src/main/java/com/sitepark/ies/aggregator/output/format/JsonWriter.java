package com.sitepark.ies.aggregator.output.format;

import com.sitepark.ies.aggregator.output.DomainObjectMapper;
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
import java.util.Map;

/**
 * Visitor that serializes an {@link Output} tree to compact JSON and writes it to a {@link Writer}.
 *
 * <p>Output rules:
 *
 * <ul>
 *   <li>{@link OutputObject} and {@link OutputListItem} become JSON objects.
 *   <li>{@link OutputList}, raw {@link Collection}, and {@code Object[]} become JSON arrays.
 *   <li>{@link Map} becomes a JSON object. Keys are converted to strings via {@code toString()}.
 *   <li>{@link PlainText}, {@link TranslatableText}, {@link PlainUri}, {@link TranslatableUri},
 *       {@link TranslatableSplitText}, and {@link RawPhpCode} are written as their {@code toString()}
 *       representation (a quoted JSON string).
 *   <li>{@link Number} and {@link Boolean} are written unquoted.
 *   <li>{@code null} is written as {@code null}.
 * </ul>
 *
 * <p>Any {@link IOException} from the underlying writer is rethrown as {@link
 * UncheckedIOException}.
 */
public final class JsonWriter extends OutputVisitor {

  private final Writer writer;

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
    super(domainObjectMapper, translations);
    this.writer = writer;
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
    write('[');
    boolean first = true;
    for (OutputListItem item : list.items()) {
      if (!first) {
        write(',');
      }
      visitListItem(item);
      first = false;
    }
    write(']');
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

  // PlainText, TranslatableText, PlainUri, TranslatableUri and TranslatableSplitText use the
  // OutputVisitor defaults, which render via the translation table and delegate to visitString() —
  // i.e. a quoted JSON string.

  @Override
  public void visitRawPhpCode(RawPhpCode value) {
    writeQuoted(value.code());
  }

  @Override
  public void visitMap(Map<?, ?> map) {
    write('{');
    boolean first = true;
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      if (!first) {
        write(',');
      }
      String key = entry.getKey() == null ? "" : entry.getKey().toString();
      writeQuoted(key);
      write(':');
      visitField(key, entry.getValue());
      first = false;
    }
    write('}');
  }

  @Override
  public void visitCollection(Collection<?> collection) {
    writeRawIterable(collection);
  }

  @Override
  public void visitArray(Object[] array) {
    writeRawArray(array);
  }

  @Override
  public void visitUnknown(Object value) {
    writeQuoted(value == null ? "" : value.toString());
  }

  private void writeJsonObject(OutputNode node) {
    write('{');
    boolean first = true;
    for (Map.Entry<String, Object> entry : flattenedEntries(node).entrySet()) {
      if (!first) {
        write(',');
      }
      writeQuoted(entry.getKey());
      write(':');
      visitField(entry.getKey(), entry.getValue());
      first = false;
    }
    write('}');
  }

  private void writeRawIterable(Iterable<?> items) {
    write('[');
    boolean first = true;
    for (Object item : items) {
      if (!first) {
        write(',');
      }
      visitField(null, item);
      first = false;
    }
    write(']');
  }

  private void writeRawArray(Object[] items) {
    write('[');
    boolean first = true;
    for (Object item : items) {
      if (!first) {
        write(',');
      }
      visitField(null, item);
      first = false;
    }
    write(']');
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
