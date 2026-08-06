package com.sitepark.ies.aggregator.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmptyRevisionTest {

  @Test
  void atIsNull() {
    assertThat(Revision.empty().at()).as("An empty revision should have no timestamp").isNull();
  }

  @Test
  void byIsNull() {
    assertThat(Revision.empty().by()).as("An empty revision should have no editor").isNull();
  }

  @Test
  void emptyIsAlwaysTheSameInstance() {
    assertThat(Revision.empty())
        .as("The empty revision is stateless and should be shared")
        .isSameAs(Revision.empty());
  }
}
