package com.sitepark.ies.aggregator.output.collect;

import static org.assertj.core.api.Assertions.assertThat;

import com.sitepark.ies.aggregator.output.DomainObjectMapper;
import com.sitepark.ies.aggregator.output.OutputList;
import com.sitepark.ies.aggregator.output.OutputObject;
import com.sitepark.ies.aggregator.value.ResolvedValue;
import com.sitepark.ies.aggregator.value.text.TranslatableSplitText;
import com.sitepark.ies.aggregator.value.text.TranslatableText;
import com.sitepark.ies.aggregator.value.uri.TranslatableUri;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TranslatableTextCollectorTest {

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
  void emptyObjectProducesEmptyList() {
    OutputObject root = new OutputObject(null, null);

    assertThat(new TranslatableTextCollector().collect(root))
        .as("Empty object should produce an empty translatable text list")
        .isEmpty();
  }

  @Test
  void collectsTranslatableTextDirectField() {
    OutputObject root = new OutputObject(null, null);
    TranslatableText tt = TranslatableText.of("hello");
    root.put("greeting", tt);

    assertThat(new TranslatableTextCollector().collect(root))
        .as("Direct TranslatableText field should be collected")
        .containsExactly(tt);
  }

  @Test
  void ignoresPlainText() {
    OutputObject root = new OutputObject(null, null);
    root.put("greeting", "hello");

    assertThat(new TranslatableTextCollector().collect(root))
        .as("Plain String value should not be collected")
        .isEmpty();
  }

  @Test
  void collectsFromNestedObject() {
    OutputObject root = new OutputObject(null, null);
    TranslatableText tt = TranslatableText.of("nested");
    root.node("meta").put("title", tt);

    assertThat(new TranslatableTextCollector().collect(root))
        .as("TranslatableText in a nested OutputObject should be collected")
        .containsExactly(tt);
  }

  @Test
  void collectsFromListItems() {
    OutputObject root = new OutputObject(null, null);
    OutputList list = root.nodeList("items");
    TranslatableText first = TranslatableText.of("one");
    TranslatableText second = TranslatableText.of("two");
    list.addItem().put("label", first);
    list.addItem().put("label", second);

    assertThat(new TranslatableTextCollector().collect(root))
        .as("TranslatableTexts inside AggregationListItems should be collected in order")
        .containsExactly(first, second);
  }

  @Test
  void collectsFromTranslatableUriPath() {
    OutputObject root = new OutputObject(null, null);
    TranslatableUri uri = TranslatableUri.of(URI.create("https://example.com/foo/bar"));
    root.put("uri", uri);
    List<TranslatableText> expected = uri.getTranslatableTextList();

    assertThat(new TranslatableTextCollector().collect(root))
        .as("TranslatableUri should contribute its translatable path segments")
        .isEqualTo(expected);
  }

  @Test
  void collectsFromTranslatableSplittedText() {
    OutputObject root = new OutputObject(null, null);
    TranslatableSplitText splitted = new TranslatableSplitText();
    TranslatableText embedded = TranslatableText.of("piece");
    splitted.add("static-prefix");
    splitted.add(embedded);
    root.put("splitted", splitted);

    assertThat(new TranslatableTextCollector().collect(root))
        .as("Embedded TranslatableText inside TranslatableSplittedText should be collected")
        .containsExactly(embedded);
  }

  @Test
  void unwrapsResolvedValueContainingTranslatableText() {
    OutputObject root = new OutputObject(null, null);
    TranslatableText tt = TranslatableText.of("wrapped");
    root.put("wrapped", ResolvedValue.of(tt));

    assertThat(new TranslatableTextCollector().collect(root))
        .as(
            "ResolvedValue wrapping a TranslatableText should be unwrapped and the inner value"
                + " collected")
        .containsExactly(tt);
  }

  @Test
  void collectsFromRawCollectionItems() {
    OutputObject root = new OutputObject(null, null);
    TranslatableText first = TranslatableText.of("one");
    TranslatableText second = TranslatableText.of("two");
    root.put("texts", List.of(first, second));

    assertThat(new TranslatableTextCollector().collect(root))
        .as("TranslatableTexts inside a raw Collection should be collected via visitCollection")
        .containsExactly(first, second);
  }

  @Test
  void collectsFromDomainObjectPropertiesWhenMapperConfigured() {
    OutputObject root = new OutputObject(null, null);
    TranslatableText label = TranslatableText.of("Hello");
    root.put("link", new Link("home", label));

    assertThat(new TranslatableTextCollector(LINK_MAPPER).collect(root))
        .as(
            "TranslatableText property of a domain object should be collected via the configured"
                + " mapper")
        .containsExactly(label);
  }
}
