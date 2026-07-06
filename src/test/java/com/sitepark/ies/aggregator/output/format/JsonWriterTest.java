package com.sitepark.ies.aggregator.output.format;

import static org.assertj.core.api.Assertions.assertThat;

import com.sitepark.ies.aggregator.output.DomainObjectMapper;
import com.sitepark.ies.aggregator.output.KeepEmpty;
import com.sitepark.ies.aggregator.output.OutputKeepIfEmpty;
import com.sitepark.ies.aggregator.output.OutputList;
import com.sitepark.ies.aggregator.output.OutputObject;
import com.sitepark.ies.aggregator.value.Emptiable;
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

  @OutputKeepIfEmpty
  public record KeptFlag() implements Emptiable {
    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public String toString() {
      return "kept";
    }
  }

  @Test
  void emptyValuesAreDropped() {
    OutputObject root = new OutputObject(null, null);
    root.put("name", "Alice");
    root.put("missing", null);
    root.put("blank", "");
    root.put("tags", List.of());
    root.put("emptyText", Text.empty());

    assertThat(render(root))
        .as("Null, empty string, empty collection and empty Emptiable fields should be dropped")
        .isEqualTo("{\"name\":\"Alice\"}");
  }

  @Test
  void emptyNestedNodesAreDropped() {
    OutputObject root = new OutputObject(null, null);
    root.put("name", "Alice");
    root.node("meta").put("blank", "");
    root.nodeList("items");

    assertThat(render(root))
        .as("A nested object/list that becomes empty after pruning should itself be dropped")
        .isEqualTo("{\"name\":\"Alice\"}");
  }

  @Test
  void emptyListItemsAreDropped() {
    OutputObject root = new OutputObject(null, null);
    OutputList list = root.nodeList("items");
    list.addItem().put("id", 1);
    list.addItem(); // empty item
    list.addItem().put("id", 2);

    assertThat(render(root))
        .as("Empty list items should be dropped while filled ones are kept")
        .isEqualTo("{\"items\":[{\"id\":1},{\"id\":2}]}");
  }

  @Test
  void typeKeepIfEmptyValueIsNotDropped() {
    OutputObject root = new OutputObject(null, null);
    root.put("flag", new KeptFlag());
    root.put("blank", "");

    assertThat(render(root))
        .as("An empty value whose type is @OutputKeepIfEmpty must be kept while other empties drop")
        .isEqualTo("{\"flag\":\"kept\"}");
  }

  @Test
  void keepEmptyWrappedValueIsRenderedWhileBareEmptyIsDropped() {
    OutputObject root = new OutputObject(null, null);
    root.put("kept", new KeepEmpty(""));
    root.put("dropped", "");

    assertThat(render(root))
        .as(
            "A KeepEmpty-wrapped empty value should be rendered while a bare empty value is"
                + " dropped")
        .isEqualTo("{\"kept\":\"\"}");
  }

  public record Detail(String title, String note) {}

  @Test
  void keptEmptyPropertyDeepInsideDomainObjectSurvives() {
    DomainObjectMapper mapper =
        value -> {
          if (value instanceof Detail detail) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("title", detail.title());
            map.put("note", new KeepEmpty(detail.note())); // property-level keep
            return map;
          }
          return null;
        };
    OutputObject root = new OutputObject(null, null);
    root.node("meta").put("detail", new Detail("", ""));

    StringWriter sw = new StringWriter();
    root.accept(new JsonWriter(sw, mapper));

    assertThat(sw.toString())
        .as(
            "A KeepEmpty property must survive nested inside a domain object while its empty"
                + " sibling drops")
        .isEqualTo("{\"meta\":{\"detail\":{\"note\":\"\"}}}");
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
