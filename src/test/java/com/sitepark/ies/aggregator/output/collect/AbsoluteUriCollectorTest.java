package com.sitepark.ies.aggregator.output.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sitepark.ies.aggregator.output.DomainObjectMapper;
import com.sitepark.ies.aggregator.output.EmptyValuePolicy;
import com.sitepark.ies.aggregator.output.KeepEmpty;
import com.sitepark.ies.aggregator.output.OutputList;
import com.sitepark.ies.aggregator.output.OutputObject;
import com.sitepark.ies.aggregator.value.ResolvedValue;
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

  @Test
  void collectsAbsoluteUriUnderAnyEmptyValuePolicy() {
    OutputObject root = new OutputObject(null, null);
    PlainUri uri = Uri.of("https://example.com/kept");
    root.put("link", uri);

    AbsoluteUriCollector collector =
        new AbsoluteUriCollector(DomainObjectMapper.NONE, EmptyValuePolicy.DROP_ALL);

    assertThat(collector.collect(root))
        .as("An absolute URI carries a scheme and is never empty, so even DROP_ALL keeps it")
        .containsExactly(uri);
  }

  @Test
  void keepEmptyWrapperDoesNotHideUri() {
    OutputObject root = new OutputObject(null, null);
    PlainUri uri = Uri.of("https://example.com/wrapped");
    root.put("link", new KeepEmpty(uri));

    assertThat(new AbsoluteUriCollector().collect(root))
        .as("A KeepEmpty wrapper is transparent, so the wrapped absolute URI is still collected")
        .containsExactly(uri);
  }

  @Test
  void collectsFromObjectArray() {
    OutputObject root = new OutputObject(null, null);
    PlainUri first = Uri.of("https://example.com/one");
    PlainUri second = Uri.of("https://example.com/two");
    root.put("links", new PlainUri[] {first, second});

    assertThat(new AbsoluteUriCollector().collect(root))
        .as("Absolute PlainUris inside an Object[] should be collected via visitArray")
        .containsExactly(first, second);
  }

  @Test
  void collectsFromMapValues() {
    OutputObject root = new OutputObject(null, null);
    PlainUri first = Uri.of("https://example.com/one");
    PlainUri second = Uri.of("https://example.com/two");
    Map<String, Object> links = new LinkedHashMap<>();
    links.put("primary", first);
    links.put("secondary", second);
    root.put("links", links);

    assertThat(new AbsoluteUriCollector().collect(root))
        .as("Absolute PlainUris inside a raw Map should be collected via visitMap")
        .containsExactly(first, second);
  }

  @Test
  void unwrapsResolvedValueContainingUri() {
    OutputObject root = new OutputObject(null, null);
    PlainUri uri = Uri.of("https://example.com/resolved");
    root.put("link", ResolvedValue.of(uri));

    assertThat(new AbsoluteUriCollector().collect(root))
        .as("ResolvedValue wrapping an absolute PlainUri should be unwrapped and the URI collected")
        .containsExactly(uri);
  }

  @Test
  void ignoresUnmappedDomainObject() {
    OutputObject root = new OutputObject(null, null);
    root.put("link", new Link("home", Uri.of("https://example.com/home")));

    assertThat(new AbsoluteUriCollector().collect(root))
        .as("Without a matching mapper the domain object stays opaque and contributes no URI")
        .isEmpty();
  }

  @Test
  void reusedCollectorDoesNotAccumulate() {
    OutputObject first = new OutputObject(null, null);
    first.put("link", Uri.of("https://example.com/first"));
    OutputObject second = new OutputObject(null, null);
    PlainUri secondUri = Uri.of("https://example.com/second");
    second.put("link", secondUri);
    AbsoluteUriCollector collector = new AbsoluteUriCollector();

    collector.collect(first);

    assertThat(collector.collect(second))
        .as("A reused collector resets its state and returns only the second tree's URIs")
        .containsExactly(secondUri);
  }

  @Test
  void returnedListIsUnmodifiable() {
    OutputObject root = new OutputObject(null, null);
    root.put("link", Uri.of("https://example.com/one"));

    List<PlainUri> collected = new AbsoluteUriCollector().collect(root);

    assertThatThrownBy(() -> collected.add(Uri.of("https://example.com/two")))
        .as("The returned snapshot should be an unmodifiable copy")
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void collectsInDocumentOrderAcrossMixedStructures() {
    OutputObject root = new OutputObject(null, null);
    PlainUri field = Uri.of("https://example.com/field");
    PlainUri nested = Uri.of("https://example.com/nested");
    PlainUri item = Uri.of("https://example.com/item");
    PlainUri element = Uri.of("https://example.com/element");
    root.put("field", field);
    root.node("meta").put("link", nested);
    root.nodeList("items").addItem().put("link", item);
    root.put("more", List.of(element));

    assertThat(new AbsoluteUriCollector().collect(root))
        .as("URIs follow document order across field, nested node, list item and collection")
        .containsExactly(field, nested, item, element);
  }
}
