package com.sitepark.ies.aggregator.output.format;

import static org.assertj.core.api.Assertions.assertThat;

import com.sitepark.ies.aggregator.output.DomainObjectMapper;
import com.sitepark.ies.aggregator.output.OutputList;
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

class JsonWriterTest {

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
    JsonWriter writer = new JsonWriter(sw);
    root.accept(writer);
    return sw.toString();
  }

  private static String renderWithDomainMapper(OutputObject root) {
    StringWriter sw = new StringWriter();
    JsonWriter writer = new JsonWriter(sw, LINK_MAPPER);
    root.accept(writer);
    return sw.toString();
  }

  private static String render(OutputObject root, Translations translations) {
    StringWriter sw = new StringWriter();
    JsonWriter writer = new JsonWriter(sw, translations);
    root.accept(writer);
    return sw.toString();
  }

  @Test
  void emptyObjectRendersAsEmptyJsonObject() {
    OutputObject root = new OutputObject(null, null);

    assertThat(render(root)).as("Empty object should render as {}").isEqualTo("{}");
  }

  @Test
  void scalarFieldsAreRenderedAsJsonValues() {
    OutputObject root = new OutputObject(null, null);
    root.put("name", "Alice");
    root.put("age", 42);
    root.put("active", true);

    assertThat(render(root))
        .as("Scalars should render as their JSON value form, comma-separated, without spaces")
        .isEqualTo("{\"name\":\"Alice\",\"age\":42,\"active\":true}");
  }

  @Test
  void nullValueRendersAsJsonNull() {
    OutputObject root = new OutputObject(null, null);
    root.put("missing", null);

    assertThat(render(root))
        .as("Null value should render as the JSON null literal")
        .isEqualTo("{\"missing\":null}");
  }

  @Test
  void nestedObjectIsRecursivelyRendered() {
    OutputObject root = new OutputObject(null, null);
    root.node("meta").put("k", "v");

    assertThat(render(root))
        .as("Nested OutputObject should produce a nested JSON object")
        .isEqualTo("{\"meta\":{\"k\":\"v\"}}");
  }

  @Test
  void listOfItemsRendersAsJsonArray() {
    OutputObject root = new OutputObject(null, null);
    OutputList list = root.nodeList("items");
    list.addItem().put("id", 1);
    list.addItem().put("id", 2);

    assertThat(render(root))
        .as("OutputList should render as a JSON array of objects")
        .isEqualTo("{\"items\":[{\"id\":1},{\"id\":2}]}");
  }

  @Test
  void stringEscapesJsonSpecialCharacters() {
    OutputObject root = new OutputObject(null, null);
    root.put("val", "back\\slash \"quote\" \n\t");

    assertThat(render(root))
        .as("JSON-specific characters should be escape-sequenced")
        .isEqualTo("{\"val\":\"back\\\\slash \\\"quote\\\" \\n\\t\"}");
  }

  @Test
  void translatableTextRendersTranslatedWhenWriterHasTranslations() {
    OutputObject root = new OutputObject(null, null);
    TranslatableText tt = TranslatableText.of("hello");
    root.put("text", tt);
    Translations de = Translations.fromIndexed(List.of(tt), List.of("hallo"), "de");

    assertThat(render(root, de))
        .as("TranslatableText should render as the translation registered in the writer's table")
        .isEqualTo("{\"text\":\"hallo\"}");
  }

  @Test
  void translatableTextWithoutTranslationsRendersSource() {
    OutputObject root = new OutputObject(null, null);
    root.put("text", TranslatableText.of("hello"));

    assertThat(render(root))
        .as("TranslatableText should render as the source string when the writer has no table")
        .isEqualTo("{\"text\":\"hello\"}");
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
        .isEqualTo("{\"link\":\"https://example.com/de/haus\"}");
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
        .isEqualTo("{\"text\":\"Hello, Welt\"}");
  }

  @Test
  void rawCollectionRendersAsJsonArray() {
    OutputObject root = new OutputObject(null, null);
    root.put("tags", List.of("a", "b", "c"));

    assertThat(render(root))
        .as("A plain Collection value should render as a JSON array via visitCollection")
        .isEqualTo("{\"tags\":[\"a\",\"b\",\"c\"]}");
  }

  @Test
  void rawArrayRendersAsJsonArray() {
    OutputObject root = new OutputObject(null, null);
    root.put("tags", new String[] {"a", "b"});

    assertThat(render(root))
        .as("A plain Object[] value should render as a JSON array via visitArray")
        .isEqualTo("{\"tags\":[\"a\",\"b\"]}");
  }

  @Test
  void domainObjectIsUnwrappedToJsonObjectWhenMapperConfigured() {
    OutputObject root = new OutputObject(null, null);
    root.put("link", new Link("home", TranslatableText.of("Hello")));

    assertThat(renderWithDomainMapper(root))
        .as(
            "Domain object should be unwrapped to a JSON object via the configured"
                + " DomainObjectMapper")
        .isEqualTo("{\"link\":{\"name\":\"home\",\"label\":\"Hello\"}}");
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
        .isEqualTo("{\"name\":\"Alice\",\"role\":\"admin\",\"level\":3}");
  }

  @Test
  void unwrappedDomainObjectIsInlinedViaDomainMapper() {
    OutputObject root = new OutputObject(null, null);
    root.put("id", 1);
    root.put("ext", new Unwrapped(new Link("home", TranslatableText.of("Hello"))));

    assertThat(renderWithDomainMapper(root))
        .as("Unwrapped domain object should be inlined flat via the DomainObjectMapper, not nested")
        .isEqualTo("{\"id\":1,\"name\":\"home\",\"label\":\"Hello\"}");
  }

  @Test
  void unwrappedEmptyContributesNoFields() {
    OutputObject root = new OutputObject(null, null);
    root.put("name", "Alice");
    root.put("ext", Unwrapped.EMPTY);

    assertThat(render(root))
        .as("Unwrapped.EMPTY should contribute no fields and leave no dangling key or comma")
        .isEqualTo("{\"name\":\"Alice\"}");
  }

  @Test
  void controlCharactersAreEscapedAsUnicode() {
    OutputObject root = new OutputObject(null, null);
    root.put("val", "\u0001");

    assertThat(render(root))
        .as("Control characters below 0x20 should be escaped as \\uXXXX")
        .isEqualTo("{\"val\":\"\\u0001\"}");
  }

  @Test
  void lineAndParagraphSeparatorsAreEscaped() {
    OutputObject root = new OutputObject(null, null);
    root.put("val", String.valueOf((char) 0x2028) + (char) 0x2029);

    assertThat(render(root))
        .as("U+2028/U+2029 should be escaped so the JSON output stays valid JavaScript")
        .isEqualTo("{\"val\":\"\\u2028\\u2029\"}");
  }

  @Test
  void textValueRendersAsQuotedString() {
    OutputObject root = new OutputObject(null, null);
    root.put("title", Text.of("hello"));

    assertThat(render(root))
        .as("A Text value object should render as a quoted JSON string via its toString()")
        .isEqualTo("{\"title\":\"hello\"}");
  }
}
