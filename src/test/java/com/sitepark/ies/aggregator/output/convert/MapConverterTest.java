package com.sitepark.ies.aggregator.output.convert;

import static org.assertj.core.api.Assertions.assertThat;

import com.sitepark.ies.aggregator.output.DomainObjectMapper;
import com.sitepark.ies.aggregator.output.OutputList;
import com.sitepark.ies.aggregator.output.OutputObject;
import com.sitepark.ies.aggregator.output.format.RawPhpCode;
import com.sitepark.ies.aggregator.value.ResolvedValue;
import com.sitepark.ies.aggregator.value.text.Text;
import com.sitepark.ies.aggregator.value.text.TranslatableText;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MapConverterTest {

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

  @Test
  void emptyObjectProducesEmptyMap() {
    OutputObject root = new OutputObject(null, null);

    assertThat(new MapConverter().toMap(root))
        .as("Empty object should produce empty map")
        .isEmpty();
  }

  @Test
  void scalarFieldsArePreserved() {
    OutputObject root = new OutputObject(null, null);
    root.put("name", "Alice");
    root.put("age", 42);
    root.put("active", true);
    Map<String, Object> expected = new LinkedHashMap<>();
    expected.put("name", "Alice");
    expected.put("age", 42);
    expected.put("active", true);

    assertThat(new MapConverter().toMap(root))
        .as("Scalar fields should be preserved as-is in the resulting map")
        .isEqualTo(expected);
  }

  @Test
  void nestedObjectIsRecursivelyConverted() {
    OutputObject root = new OutputObject(null, null);
    root.node("metadata").put("created", "2026-05-27");

    assertThat(new MapConverter().toMap(root))
        .as("Nested OutputObject should be converted recursively into a nested map")
        .isEqualTo(Map.of("metadata", Map.of("created", "2026-05-27")));
  }

  @Test
  void listOfItemsIsConvertedToListOfMaps() {
    OutputObject root = new OutputObject(null, null);
    OutputList list = root.nodeList("items");
    list.addItem().put("id", 1);
    list.addItem().put("id", 2);

    assertThat(new MapConverter().toMap(root))
        .as("OutputList should be converted to a list of maps")
        .isEqualTo(Map.of("items", List.of(Map.of("id", 1), Map.of("id", 2))));
  }

  @Test
  void textValueIsKeptAsTypedObject() {
    OutputObject root = new OutputObject(null, null);
    Text text = Text.of("hello");
    root.put("text", text);

    assertThat(new MapConverter().toMap(root).get("text"))
        .as("Text value should remain a Text instance, not be stringified")
        .isSameAs(text);
  }

  @Test
  void translatableTextValueIsKeptAsTypedObject() {
    OutputObject root = new OutputObject(null, null);
    TranslatableText translatable = TranslatableText.of("source");
    root.put("translatable", translatable);

    assertThat(new MapConverter().toMap(root).get("translatable"))
        .as("TranslatableText should remain a TranslatableText instance")
        .isSameAs(translatable);
  }

  @Test
  void rawPhpCodeIsKeptAsTypedObject() {
    OutputObject root = new OutputObject(null, null);
    RawPhpCode code = new RawPhpCode("foo()");
    root.put("callable", code);

    assertThat(new MapConverter().toMap(root).get("callable"))
        .as("RawPhpCode should remain a RawPhpCode instance")
        .isSameAs(code);
  }

  @Test
  void resolvedValueIsUnwrapped() {
    OutputObject root = new OutputObject(null, null);
    root.put("filled", ResolvedValue.of("hello"));

    assertThat(new MapConverter().toMap(root).get("filled"))
        .as("ResolvedValue should be unwrapped to its inner value")
        .isEqualTo("hello");
  }

  @Test
  void emptyResolvedValueBecomesNull() {
    OutputObject root = new OutputObject(null, null);
    root.put("empty", ResolvedValue.empty());

    assertThat(new MapConverter().toMap(root).get("empty"))
        .as("Empty ResolvedValue should be unwrapped to null")
        .isNull();
  }

  @Test
  void rootOutputListProducesList() {
    OutputObject parent = new OutputObject(null, null);
    OutputList list = parent.nodeList("dummy");
    list.addItem().put("id", 7);

    assertThat(new MapConverter().toList(list))
        .as("Root OutputList should be converted to a list of maps")
        .isEqualTo(List.of(Map.of("id", 7)));
  }

  @Test
  void rawCollectionIsConvertedToNestedListPreservingValueClasses() {
    OutputObject root = new OutputObject(null, null);
    TranslatableText tt = TranslatableText.of("Hello");
    root.put("items", List.of(tt));

    assertThat(new MapConverter().toMap(root).get("items"))
        .as(
            "A raw Collection value should become a List with typed value-class instances"
                + " preserved")
        .isEqualTo(List.of(tt));
  }

  @Test
  void domainObjectIsUnwrappedToNestedMapWhenMapperConfigured() {
    OutputObject root = new OutputObject(null, null);
    TranslatableText label = TranslatableText.of("Hello");
    root.put("link", new Link("home", label));

    Map<String, Object> result = new MapConverter(LINK_MAPPER).toMap(root);

    assertThat(result.get("link"))
        .as(
            "Domain object should be unwrapped to a nested map with typed property values"
                + " preserved")
        .isEqualTo(Map.of("name", "home", "label", label));
  }
}
