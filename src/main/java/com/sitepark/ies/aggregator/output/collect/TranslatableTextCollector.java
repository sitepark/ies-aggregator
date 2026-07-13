package com.sitepark.ies.aggregator.output.collect;

import com.sitepark.ies.aggregator.output.DomainObjectMapper;
import com.sitepark.ies.aggregator.output.Output;
import com.sitepark.ies.aggregator.output.OutputVisitor;
import com.sitepark.ies.aggregator.value.text.TranslatableContainer;
import com.sitepark.ies.aggregator.value.text.TranslatableSplitText;
import com.sitepark.ies.aggregator.value.text.TranslatableText;
import com.sitepark.ies.aggregator.value.uri.TranslatableUri;
import java.util.ArrayList;
import java.util.List;

/**
 * Visitor that collects every {@link TranslatableText} reachable from an {@link Output} tree —
 * including the ones contributed by {@link TranslatableUri} and {@link TranslatableSplitText} via
 * their {@code getTranslatableTextList()} hooks.
 *
 * <p>The collected list is order-preserving (document order of traversal) and contains the original
 * {@code TranslatableText} references. It is the index bridge of the translation lifecycle: extract
 * the source texts (with {@code SourceText.of}), send them to a translation service, and map the
 * index-corresponding result back via {@code Translations.fromIndexed} (see {@code
 * docs/translations.md}). The collector is language-agnostic and does not mutate the tree.
 */
public final class TranslatableTextCollector extends OutputVisitor {

  private final List<TranslatableText> collected = new ArrayList<>();

  /** Creates a collector without a domain object mapper. */
  public TranslatableTextCollector() {
    super();
  }

  /**
   * Creates a collector with a custom domain object mapper.
   *
   * @param domainObjectMapper the mapper for unwrapping domain objects
   */
  public TranslatableTextCollector(DomainObjectMapper domainObjectMapper) {
    super(domainObjectMapper);
  }

  /**
   * Traverses {@code root} and returns all reachable {@link TranslatableText} instances in document
   * order.
   *
   * @param root the output node to traverse
   * @return an unmodifiable snapshot of the collected texts
   */
  public List<TranslatableText> collect(Output root) {
    this.collected.clear();
    root.accept(this);
    return List.copyOf(this.collected);
  }

  @Override
  public void visitTranslatableText(TranslatableText value) {
    this.collected.add(value);
  }

  @Override
  public void visitTranslatableUri(TranslatableUri value) {
    this.collected.addAll(value.getTranslatableTextList());
  }

  @Override
  public void visitTranslatableContainer(TranslatableContainer value) {
    this.collected.addAll(value.getTranslatableTextList());
  }
}
