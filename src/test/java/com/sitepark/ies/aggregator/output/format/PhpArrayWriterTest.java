package com.sitepark.ies.aggregator.output.format;

import static org.assertj.core.api.Assertions.assertThat;

import com.sitepark.ies.aggregator.output.DomainObjectMapper;
import com.sitepark.ies.aggregator.output.OutputList;
import com.sitepark.ies.aggregator.output.OutputListItem;
import com.sitepark.ies.aggregator.output.OutputObject;
import com.sitepark.ies.aggregator.output.Unwrapped;
import com.sitepark.ies.aggregator.value.text.Text;
import com.sitepark.ies.aggregator.value.text.TranslatableSplitText;
import com.sitepark.ies.aggregator.value.text.TranslatableText;
import com.sitepark.ies.aggregator.value.text.Translations;
import com.sitepark.ies.aggregator.value.uri.TranslatableUri;
import java.io.StringWriter;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PhpArrayWriterTest {

  public record Link(String name, TranslatableText label) {}

  private static final DomainObjectMapper LINK_MAPPER =
      value -> {
        if (value instanceof Link link) {
          Map<String, Object> map = new LinkedHashMap<>();
          map.put("name", link.name());
          map.put("label", link.label());
          return map;
        }
        return null;
      };

  private static String render(OutputObject root) {
    StringWriter sw = new StringWriter();
    PhpArrayWriter writer = new PhpArrayWriter(sw);
    root.accept(writer);
    return sw.toString();
  }

  private static String render(OutputObject root, Translations translations) {
    StringWriter sw = new StringWriter();
    PhpArrayWriter writer = new PhpArrayWriter(sw, translations);
    root.accept(writer);
    return sw.toString();
  }

  private static String renderWithDomainMapper(OutputObject root) {
    StringWriter sw = new StringWriter();
    PhpArrayWriter writer = new PhpArrayWriter(sw, LINK_MAPPER);
    root.accept(writer);
    return sw.toString();
  }

  @Test
  void emptyObjectRendersAsEmptyArray() {
    OutputObject root = new OutputObject(null, null);

    assertThat(render(root)).as("Empty object should render as []").isEqualTo("[]");
  }

  @Test
  void singleStringFieldIsQuotedAndIndented() {
    OutputObject root = new OutputObject(null, null);
    root.put("name", "Alice");

    assertThat(render(root))
        .as("Single string field should be quoted and indented with one tab")
        .isEqualTo("[\n\t\"name\" => \"Alice\"\n]");
  }

  @Test
  void multipleFieldsAreSeparatedByComma() {
    OutputObject root = new OutputObject(null, null);
    root.put("name", "Alice");
    root.put("age", 42);

    assertThat(render(root))
        .as("Multiple fields should be separated by comma and newline")
        .isEqualTo("[\n\t\"name\" => \"Alice\",\n\t\"age\" => 42\n]");
  }

  @Test
  void numberIsWrittenWithoutQuotes() {
    OutputObject root = new OutputObject(null, null);
    root.put("count", 7);

    assertThat(render(root))
        .as("Number should be written without quotes")
        .isEqualTo("[\n\t\"count\" => 7\n]");
  }

  @Test
  void booleanIsWrittenWithoutQuotes() {
    OutputObject root = new OutputObject(null, null);
    root.put("active", true);

    assertThat(render(root))
        .as("Boolean should be written as bare true/false")
        .isEqualTo("[\n\t\"active\" => true\n]");
  }

  @Test
  void nullValueIsWrittenAsNullLiteral() {
    OutputObject root = new OutputObject(null, null);
    root.put("missing", null);

    assertThat(render(root))
        .as("Null value should be written as the bare null literal")
        .isEqualTo("[\n\t\"missing\" => null\n]");
  }

  @Test
  void rawPhpCodeIsWrittenVerbatim() {
    OutputObject root = new OutputObject(null, null);
    root.put("callable", new RawPhpCode("foo()"));

    assertThat(render(root))
        .as("RawPhpCode value should be emitted verbatim without quoting")
        .isEqualTo("[\n\t\"callable\" => foo()\n]");
  }

  @Test
  void nestedObjectIncreasesIndentation() {
    OutputObject root = new OutputObject(null, null);
    root.node("meta").put("k", "v");

    assertThat(render(root))
        .as("Nested OutputObject should increase indentation by one tab")
        .isEqualTo("[\n\t\"meta\" => [\n\t\t\"k\" => \"v\"\n\t]\n]");
  }

  @Test
  void multiItemListIsRenderedWithNewlines() {
    OutputObject root = new OutputObject(null, null);
    OutputList list = root.nodeList("items");
    list.addItem().put("id", 1);
    list.addItem().put("id", 2);

    assertThat(render(root))
        .as("Multi-item list should render each item on its own indented line")
        .isEqualTo(
            """
            [
            \t"items" => [
            \t\t[
            \t\t\t"id" => 1
            \t\t],
            \t\t[
            \t\t\t"id" => 2
            \t\t]
            \t]
            ]\
            """);
  }

  @Test
  void singleItemListSkipsNewlineBeforeItem() {
    OutputObject root = new OutputObject(null, null);
    OutputList list = root.nodeList("items");
    list.addItem().put("id", 1);

    assertThat(render(root))
        .as("Single-item list should not introduce a newline before its only element")
        .isEqualTo("[\n\t\"items\" => [[\n\t\t\"id\" => 1\n\t]]\n]");
  }

  @Test
  void emptyListRendersAsEmptyArray() {
    OutputObject root = new OutputObject(null, null);
    root.nodeList("items");

    assertThat(render(root))
        .as("Empty OutputList should render as []")
        .isEqualTo("[\n\t\"items\" => []\n]");
  }

  @Test
  void unwrappedMapValueIsInlinedAsSiblingFields() {
    OutputObject root = new OutputObject(null, null);
    root.put("name", "Alice");
    Map<String, Object> extension = new LinkedHashMap<>();
    extension.put("role", "admin");
    extension.put("level", 3);
    root.put("ext", new Unwrapped(extension));

    assertThat(render(root))
        .as("Unwrapped map should be inlined flat as sibling fields, not nested under its key")
        .isEqualTo(
            """
            [
            \t"name" => "Alice",
            \t"role" => "admin",
            \t"level" => 3
            ]\
            """);
  }

  @Test
  void unwrappedDomainObjectIsInlinedViaDomainMapper() {
    OutputObject root = new OutputObject(null, null);
    root.put("id", 1);
    root.put("ext", new Unwrapped(new Link("home", TranslatableText.of("Hello"))));

    assertThat(renderWithDomainMapper(root))
        .as("Unwrapped domain object should be inlined flat via the DomainObjectMapper, not nested")
        .isEqualTo(
            """
            [
            \t"id" => 1,
            \t"name" => "home",
            \t"label" => "Hello"
            ]\
            """);
  }

  @Test
  void unwrappedEmptyContributesNoFields() {
    OutputObject root = new OutputObject(null, null);
    root.put("name", "Alice");
    root.put("ext", Unwrapped.EMPTY);

    assertThat(render(root))
        .as("Unwrapped.EMPTY should contribute no fields and leave no dangling key or comma")
        .isEqualTo("[\n\t\"name\" => \"Alice\"\n]");
  }

  @Test
  void stringEscapesAllSpecialCharacters() {
    OutputObject root = new OutputObject(null, null);
    root.put("val", "back\\slash \"quote\" \b\t\n\f\r$var");

    assertThat(render(root))
        .as("All PHP-special characters should be escape-sequenced")
        .isEqualTo("[\n\t\"val\" => \"back\\\\slash \\\"quote\\\" \\b\\t\\n\\f\\r\\$var\"\n]");
  }

  @Test
  void controlCharactersAreEscapedAsHex() {
    OutputObject root = new OutputObject(null, null);
    root.put("val", "");

    assertThat(render(root))
        .as("Control characters below 0x20 should be escaped as \\xXX, not emitted as raw bytes")
        .isEqualTo("[\n\t\"val\" => \"\\x01\"\n]");
  }

  @Test
  void textValueRendersAsString() {
    OutputObject root = new OutputObject(null, null);
    root.put("title", Text.of("hello"));

    assertThat(render(root))
        .as("A Text value object should render as a quoted string via its toString()")
        .isEqualTo("[\n\t\"title\" => \"hello\"\n]");
  }

  @Test
  void translatableTextRendersAsString() {
    OutputObject root = new OutputObject(null, null);
    root.put("text", TranslatableText.of("hello"));

    assertThat(render(root))
        .as("TranslatableText without translations should render as its source text")
        .isEqualTo("[\n\t\"text\" => \"hello\"\n]");
  }

  @Test
  void translatableTextRendersTranslatedWhenWriterHasTranslations() {
    OutputObject root = new OutputObject(null, null);
    TranslatableText tt = TranslatableText.of("hello");
    root.put("text", tt);
    Translations de = Translations.fromIndexed(List.of(tt), List.of("hallo"), "de");

    assertThat(render(root, de))
        .as("TranslatableText should render the translation registered in the writer's table")
        .isEqualTo("[\n\t\"text\" => \"hallo\"\n]");
  }

  @Test
  void translatableUriRendersTranslatedWithLangPrefix() {
    OutputObject root = new OutputObject(null, null);
    TranslatableUri uri = TranslatableUri.of(URI.create("https://example.com/foo"));
    root.put("link", uri);
    Translations de =
        Translations.fromIndexed(uri.getTranslatableTextList(), List.of("haus"), "de");

    assertThat(render(root, de))
        .as("TranslatableUri should render through the writer's table with the language prefix")
        .isEqualTo("[\n\t\"link\" => \"https://example.com/de/haus\"\n]");
  }

  @Test
  void translatableSplitTextRendersTranslatedSegments() {
    OutputObject root = new OutputObject(null, null);
    TranslatableSplitText splitted = new TranslatableSplitText();
    TranslatableText embedded = TranslatableText.of("world");
    splitted.add("Hello, ");
    splitted.add(embedded);
    root.put("text", splitted);
    Translations de = Translations.fromIndexed(List.of(embedded), List.of("Welt"), "de");

    assertThat(render(root, de))
        .as("TranslatableSplitText should render its segments through the writer's table")
        .isEqualTo("[\n\t\"text\" => \"Hello, Welt\"\n]");
  }

  @Test
  void rawCollectionRendersAsIndexedArray() {
    OutputObject root = new OutputObject(null, null);
    root.put("tags", List.of("a", "b"));

    assertThat(render(root))
        .as("A plain Collection value should render as a PHP indexed array via visitCollection")
        .isEqualTo("[\n\t\"tags\" => [\n\t\t\"a\",\n\t\t\"b\"\n\t]\n]");
  }

  @Test
  void rawArrayRendersAsIndexedArray() {
    OutputObject root = new OutputObject(null, null);
    root.put("tags", new String[] {"a", "b"});

    assertThat(render(root))
        .as("A plain Object[] value should render as a PHP indexed array via visitArray")
        .isEqualTo("[\n\t\"tags\" => [\n\t\t\"a\",\n\t\t\"b\"\n\t]\n]");
  }

  @Test
  void domainObjectIsUnwrappedToAssociativeArrayWhenMapperConfigured() {
    OutputObject root = new OutputObject(null, null);
    root.put("link", new Link("home", TranslatableText.of("Hello")));

    assertThat(renderWithDomainMapper(root))
        .as(
            "Domain object should be unwrapped to a PHP associative array via the configured"
                + " DomainObjectMapper")
        .isEqualTo(
            "[\n\t\"link\" => [\n\t\t\"name\" => \"home\",\n\t\t\"label\" => \"Hello\"\n\t]\n]");
  }

  @Test
  void singleEntryListItemRendersInline() {
    OutputObject parent = new OutputObject(null, null);
    OutputList list = parent.nodeList("dummy");
    OutputListItem item = list.addItem();
    item.put("k", "v");
    StringWriter sw = new StringWriter();

    list.accept(new PhpArrayWriter(sw));

    assertThat(sw.toString())
        .as("Top-level single-item list should also use inline single-item optimization")
        .isEqualTo("[[\n\t\"k\" => \"v\"\n]]");
  }
}
