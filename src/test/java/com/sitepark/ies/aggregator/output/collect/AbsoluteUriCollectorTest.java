package com.sitepark.ies.aggregator.output.collect;

import static org.assertj.core.api.Assertions.assertThat;

import com.sitepark.ies.aggregator.output.DomainObjectMapper;
import com.sitepark.ies.aggregator.output.OutputList;
import com.sitepark.ies.aggregator.output.OutputObject;
import com.sitepark.ies.aggregator.value.uri.PlainUri;
import com.sitepark.ies.aggregator.value.uri.TranslatableUri;
import com.sitepark.ies.aggregator.value.uri.Uri;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AbsoluteUriCollectorTest {

  public record Link(String name, Uri uri) {}

  private static final DomainObjectMapper LINK_MAPPER =
      value -> {
        if (value instanceof Link link) {
          Map<String, Object> map = new LinkedHashMap<>();
          map.put("name", link.name());
          map.put("uri", link.uri());
          return map;
        }
        return null;
      };

  @Test
  void emptyObjectProducesEmptyList() {
    OutputObject root = new OutputObject(null, null);

    assertThat(new AbsoluteUriCollector().collect(root))
        .as("Empty object should produce an empty absolute URI list")
        .isEmpty();
  }

  @Test
  void collectsAbsolutePlainUriDirectField() {
    OutputObject root = new OutputObject(null, null);
    PlainUri uri = Uri.of("https://example.com/foo/bar");
    root.put("link", uri);

    assertThat(new AbsoluteUriCollector().collect(root))
        .as("Absolute PlainUri field should be collected")
        .containsExactly(uri);
  }

  @Test
  void ignoresRelativePlainUri() {
    OutputObject root = new OutputObject(null, null);
    root.put("link", Uri.of("/foo/bar"));

    assertThat(new AbsoluteUriCollector().collect(root))
        .as("Relative PlainUri (no scheme) should not be collected")
        .isEmpty();
  }

  @Test
  void ignoresTranslatableUri() {
    OutputObject root = new OutputObject(null, null);
    root.put("link", TranslatableUri.of(URI.create("https://example.com/foo/bar")));

    assertThat(new AbsoluteUriCollector().collect(root))
        .as("TranslatableUri should not be collected, only PlainUri")
        .isEmpty();
  }

  @Test
  void collectsFromNestedObject() {
    OutputObject root = new OutputObject(null, null);
    PlainUri uri = Uri.of("https://example.com/nested");
    root.node("meta").put("link", uri);

    assertThat(new AbsoluteUriCollector().collect(root))
        .as("Absolute PlainUri in a nested OutputObject should be collected")
        .containsExactly(uri);
  }

  @Test
  void collectsFromListItemsInOrder() {
    OutputObject root = new OutputObject(null, null);
    OutputList list = root.nodeList("items");
    PlainUri first = Uri.of("https://example.com/one");
    PlainUri second = Uri.of("https://example.com/two");
    list.addItem().put("link", first);
    list.addItem().put("link", second);

    assertThat(new AbsoluteUriCollector().collect(root))
        .as("Absolute PlainUris inside list items should be collected in order")
        .containsExactly(first, second);
  }

  @Test
  void collectsFromRawCollectionItems() {
    OutputObject root = new OutputObject(null, null);
    PlainUri first = Uri.of("https://example.com/one");
    PlainUri second = Uri.of("https://example.com/two");
    root.put("links", List.of(first, second));

    assertThat(new AbsoluteUriCollector().collect(root))
        .as("Absolute PlainUris inside a raw Collection should be collected via visitCollection")
        .containsExactly(first, second);
  }

  @Test
  void deduplicatesEqualUrisPreservingFirstOccurrence() {
    OutputObject root = new OutputObject(null, null);
    PlainUri first = Uri.of("https://example.com/one");
    PlainUri duplicate = Uri.of("https://example.com/one");
    PlainUri second = Uri.of("https://example.com/two");
    root.put("a", first);
    root.put("b", second);
    root.put("c", duplicate);

    assertThat(new AbsoluteUriCollector().collect(root))
        .as("Equal absolute URIs should be collected once, in first-occurrence order")
        .containsExactly(first, second);
  }

  @Test
  void collectsFromDomainObjectPropertiesWhenMapperConfigured() {
    OutputObject root = new OutputObject(null, null);
    PlainUri uri = Uri.of("https://example.com/home");
    root.put("link", new Link("home", uri));

    assertThat(new AbsoluteUriCollector(LINK_MAPPER).collect(root))
        .as("Absolute PlainUri property of a domain object should be collected via the mapper")
        .containsExactly(uri);
  }
}
