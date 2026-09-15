package com.sitepark.ies.aggregator.output.convert;

import static org.assertj.core.api.Assertions.assertThat;

import com.sitepark.ies.aggregator.output.DomainObjectMapper;
import com.sitepark.ies.aggregator.output.OutputList;
import com.sitepark.ies.aggregator.output.OutputObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * Deciding whether a domain object renders empty means mapping it, and the visitor writes that same
 * map moments later. These tests pin down that it is mapped once, not once per decision, and that
 * carrying the map along changes nothing about what is written.
 */
class DomainObjectMappedOnceTest {

  record Link(String name) {}

  private final AtomicInteger mappings = new AtomicInteger();

  /** Maps a {@link Link} and counts how often it was asked to. */
  private final DomainObjectMapper countingMapper =
      value -> {
        if (value instanceof Link link) {
          this.mappings.incrementAndGet();
          Map<String, Object> map = new LinkedHashMap<>();
          map.put("name", link.name());
          return map;
        }
        return null;
      };

  private Map<String, Object> convert(OutputObject root) {
    return new MapConverter(this.countingMapper).toMap(root);
  }

  @Test
  void testDomainObjectInAFieldIsMappedOnce() {
    OutputObject root = new OutputObject(null, null);
    root.put("link", new Link("home"));

    assertThat(convert(root)).as("converted output").containsKey("link");
    assertThat(this.mappings)
        .as("times the domain object was mapped while writing one field")
        .hasValue(1);
  }

  @Test
  void testDomainObjectInACollectionIsMappedOnce() {
    OutputObject root = new OutputObject(null, null);
    root.put("links", List.of(new Link("home")));

    convert(root);

    assertThat(this.mappings)
        .as("times the domain object was mapped while writing a collection")
        .hasValue(1);
  }

  @Test
  void testDomainObjectInAMapIsMappedOnce() {
    OutputObject root = new OutputObject(null, null);
    root.put("byName", Map.of("home", new Link("home")));

    convert(root);

    assertThat(this.mappings)
        .as("times the domain object was mapped while writing a map")
        .hasValue(1);
  }

  @Test
  void testDomainObjectInAListItemIsMappedOnce() {
    OutputObject root = new OutputObject(null, null);
    OutputList list = root.nodeList("list");
    list.addItem().put("link", new Link("home"));

    convert(root);

    assertThat(this.mappings)
        .as("times the domain object was mapped while writing a list item")
        .hasValue(1);
  }

  @Test
  void testEachDomainObjectIsMappedOnce() {
    OutputObject root = new OutputObject(null, null);
    root.put("links", List.of(new Link("home"), new Link("imprint")));

    convert(root);

    assertThat(this.mappings).as("times two domain objects were mapped").hasValue(2);
  }

  @Test
  void testEmptyDomainObjectIsStillDropped() {
    OutputObject root = new OutputObject(null, null);
    root.put("link", new Link(""));
    root.put("kept", "value");

    // The emptiness decision must not change: a domain object whose properties all render empty
    // is dropped, carrier or not.
    assertThat(convert(root))
        .as("output of a domain object whose only property is empty")
        .containsOnlyKeys("kept");
  }

  @Test
  void testWrittenValueIsTheMappedOne() {
    OutputObject root = new OutputObject(null, null);
    root.put("link", new Link("home"));
    Map<String, Object> expected = new LinkedHashMap<>();
    expected.put("link", Map.of("name", "home"));

    // The carrier must never reach the output: what lands there is the property map.
    assertThat(convert(root)).as("converted output").isEqualTo(expected);
  }
}
