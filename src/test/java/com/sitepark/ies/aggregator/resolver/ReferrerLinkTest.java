package com.sitepark.ies.aggregator.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class ReferrerLinkTest {

  @Test
  void keepsTheReferrerAndTheScopeOfTheLink() {
    EntityResolver parent = mock();
    Resolver scope = mock();

    ReferrerLink link = new ReferrerLink(parent, scope);

    assertThat(link.parent()).as("the referring entity").isSameAs(parent);
    assertThat(link.linkScope()).as("the scope the link is stored in").isSameAs(scope);
  }

  @Test
  @SuppressWarnings("NullAway")
  void rejectsAMissingReferrer() {
    Resolver scope = mock();

    assertThatThrownBy(() -> new ReferrerLink(null, scope))
        .as("a link without its referrer")
        .isInstanceOf(NullPointerException.class)
        .hasMessage("parent");
  }

  @Test
  @SuppressWarnings("NullAway")
  void rejectsAMissingScope() {
    EntityResolver parent = mock();

    assertThatThrownBy(() -> new ReferrerLink(parent, null))
        .as("a link without the scope it is stored in")
        .isInstanceOf(NullPointerException.class)
        .hasMessage("linkScope");
  }
}
