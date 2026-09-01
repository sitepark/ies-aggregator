package com.sitepark.ies.aggregator.output.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sitepark.ies.aggregator.output.DomainObjectMapper;
import com.sitepark.ies.aggregator.output.EmptyValuePolicy;
import com.sitepark.ies.aggregator.output.KeepEmpty;
import com.sitepark.ies.aggregator.output.OutputList;
import com.sitepark.ies.aggregator.output.OutputObject;
import com.sitepark.ies.aggregator.value.ResolvedValue;
import com.sitepark.ies.aggregator.value.text.TranslatableContainer;
import com.sitepark.ies.aggregator.value.text.TranslatableSplitText;
import com.sitepark.ies.aggregator.value.text.TranslatableText;
import com.sitepark.ies.aggregator.value.text.Translations;
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

  /** Minimal TranslatableContainer that is neither a TranslatableSplitText nor a TranslatableUri. */
  record SingleTextContainer(TranslatableText text) implements TranslatableContainer {
    @Override
    public List<TranslatableText> getTranslatableTextList() {
      return List.of(this.text);
    }

    @Override
    public Object render(Translations translations) {
      return translations.translationFor(this.text);
    }
  }

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
  void duplicateSourceTextsAreCollectedSeparately() {
    OutputObject root = new OutputObject(null, null);
    TranslatableText first = TranslatableText.of("same");
    TranslatableText second = TranslatableText.of("same");
    root.put("a", first);
    root.node("meta").put("b", second);

    List<TranslatableText> collected = new TranslatableTextCollector().collect(root);

    assertThat(collected)
        .as(
            "Value-equal texts must not be deduplicated: each occurrence is its own translation"
                + " slot")
        .hasSize(2);
    assertThat(collected.get(0))
        .as("The first collected text should be the exact instance from the tree")
        .isSameAs(first);
    assertThat(collected.get(1))
        .as("The second collected text should be the exact instance from the tree")
        .isSameAs(second);
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

  @Test
  void emptyTranslatableTextIsDroppedByDefault() {
    OutputObject root = new OutputObject(null, null);
    root.put("greeting", TranslatableText.of(""));

    assertThat(new TranslatableTextCollector().collect(root))
        .as("An empty text is pruned before dispatch, so the default policy collects nothing")
        .isEmpty();
  }

  @Test
  void policyKeepTypesCollectsEmptyTranslatableText() {
    OutputObject root = new OutputObject(null, null);
    TranslatableText empty = TranslatableText.of("");
    root.put("greeting", empty);

    TranslatableTextCollector collector =
        new TranslatableTextCollector(
            DomainObjectMapper.NONE, EmptyValuePolicy.keepTypes(TranslatableText.class));

    assertThat(collector.collect(root))
        .as("A policy keeping TranslatableText collects the empty text as its own translation slot")
        .containsExactly(empty);
  }

  @Test
  void keepEmptyWrapperKeepsText() {
    OutputObject root = new OutputObject(null, null);
    TranslatableText empty = TranslatableText.of("");
    root.put("greeting", new KeepEmpty(empty));

    assertThat(new TranslatableTextCollector().collect(root))
        .as("A KeepEmpty wrapper keeps one empty text without changing the global policy")
        .containsExactly(empty);
  }

  @Test
  void collectedListMatchesFromIndexedContract() {
    OutputObject root = new OutputObject(null, null);
    TranslatableText first = TranslatableText.of("one");
    TranslatableText second = TranslatableText.of("two");
    root.put("a", first);
    root.node("meta").put("b", second);
    List<TranslatableText> collected = new TranslatableTextCollector().collect(root);

    Translations translations = Translations.fromIndexed(collected, List.of("eins", "zwei"), "de");

    assertThat(translations.translationFor(first))
        .as("The first collected text should map to the translation at the same index")
        .isEqualTo("eins");
    assertThat(translations.translationFor(second))
        .as("The second collected text should map to the translation at the same index")
        .isEqualTo("zwei");
  }

  @Test
  void customTranslatableContainerContributesTexts() {
    OutputObject root = new OutputObject(null, null);
    TranslatableText embedded = TranslatableText.of("inside");
    root.put("custom", new SingleTextContainer(embedded));

    assertThat(new TranslatableTextCollector().collect(root))
        .as("Any TranslatableContainer contributes its texts, not only the bundled implementations")
        .containsExactly(embedded);
  }

  @Test
  void collectsFromObjectArray() {
    OutputObject root = new OutputObject(null, null);
    TranslatableText first = TranslatableText.of("one");
    TranslatableText second = TranslatableText.of("two");
    root.put("texts", new TranslatableText[] {first, second});

    assertThat(new TranslatableTextCollector().collect(root))
        .as("TranslatableTexts inside an Object[] should be collected via visitArray")
        .containsExactly(first, second);
  }

  @Test
  void collectsFromMapValues() {
    OutputObject root = new OutputObject(null, null);
    TranslatableText first = TranslatableText.of("one");
    TranslatableText second = TranslatableText.of("two");
    Map<String, Object> texts = new LinkedHashMap<>();
    texts.put("headline", first);
    texts.put("teaser", second);
    root.put("texts", texts);

    assertThat(new TranslatableTextCollector().collect(root))
        .as("TranslatableTexts inside a raw Map should be collected via visitMap")
        .containsExactly(first, second);
  }

  @Test
  void ignoresUnmappedDomainObject() {
    OutputObject root = new OutputObject(null, null);
    root.put("link", new Link("home", TranslatableText.of("Hello")));

    assertThat(new TranslatableTextCollector().collect(root))
        .as("Without a matching mapper the domain object stays opaque and contributes no text")
        .isEmpty();
  }

  @Test
  void reusedCollectorDoesNotAccumulate() {
    OutputObject first = new OutputObject(null, null);
    first.put("label", TranslatableText.of("first"));
    OutputObject second = new OutputObject(null, null);
    TranslatableText secondText = TranslatableText.of("second");
    second.put("label", secondText);
    TranslatableTextCollector collector = new TranslatableTextCollector();

    collector.collect(first);

    assertThat(collector.collect(second))
        .as("A reused collector resets its state and returns only the second tree's texts")
        .containsExactly(secondText);
  }

  @Test
  void returnedListIsUnmodifiable() {
    OutputObject root = new OutputObject(null, null);
    root.put("label", TranslatableText.of("one"));

    List<TranslatableText> collected = new TranslatableTextCollector().collect(root);

    assertThatThrownBy(() -> collected.add(TranslatableText.of("two")))
        .as("The returned snapshot should be an unmodifiable copy")
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void collectsInDocumentOrderAcrossMixedStructures() {
    OutputObject root = new OutputObject(null, null);
    TranslatableText field = TranslatableText.of("field");
    TranslatableText nested = TranslatableText.of("nested");
    TranslatableText item = TranslatableText.of("item");
    TranslatableText element = TranslatableText.of("element");
    root.put("field", field);
    root.node("meta").put("label", nested);
    root.nodeList("items").addItem().put("label", item);
    root.put("more", List.of(element));

    assertThat(new TranslatableTextCollector().collect(root))
        .as("Texts follow document order across field, nested node, list item and collection")
        .containsExactly(field, nested, item, element);
  }
}
