package com.sitepark.ies.aggregator.output.collect;

import com.sitepark.ies.aggregator.output.DomainObjectMapper;
import com.sitepark.ies.aggregator.output.Output;
import com.sitepark.ies.aggregator.output.OutputVisitor;
import com.sitepark.ies.aggregator.value.uri.PlainUri;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Visitor that collects every absolute {@link PlainUri} reachable from an {@link Output} tree.
 *
 * <p>Only non-translatable {@link PlainUri} values that are {@link PlainUri#isAbsolute() absolute}
 * (i.e. carry a scheme component) are collected. Translatable URIs are language-dependent, usually
 * internal paths and are therefore ignored.
 *
 * <p>The collected list is deduplicated (by URI value) and order-preserving (document order of
 * first occurrence). It is intended as the input for a link checker that periodically verifies
 * whether the collected URLs are still valid. The collector does not mutate the tree.
 */
public final class AbsoluteUriCollector extends OutputVisitor {

  private final Set<PlainUri> collected = new LinkedHashSet<>();

  /** Creates a collector without a domain object mapper. */
  public AbsoluteUriCollector() {
    super();
  }

  /**
   * Creates a collector with a custom domain object mapper.
   *
   * @param domainObjectMapper the mapper for unwrapping domain objects
   */
  public AbsoluteUriCollector(DomainObjectMapper domainObjectMapper) {
    super(domainObjectMapper);
  }

  /**
   * Traverses {@code root} and returns all reachable absolute {@link PlainUri} instances,
   * deduplicated and in document order of first occurrence.
   *
   * @param root the output node to traverse
   * @return an unmodifiable snapshot of the collected absolute URIs
   */
  public List<PlainUri> collect(Output root) {
    this.collected.clear();
    root.accept(this);
    return List.copyOf(this.collected);
  }

  @Override
  public void visitPlainUri(PlainUri value) {
    if (value.isAbsolute()) {
      this.collected.add(value);
    }
  }
}
