package com.sitepark.ies.aggregator.output.format;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class JsonIndentTest {

  @Test
  void noneIsCompact() {
    assertThat(JsonIndent.NONE.isCompact())
        .as("NONE should render everything on one line")
        .isTrue();
  }

  @Test
  void ofIndentsWithSpacesFromColumnZero() {
    JsonIndent indent = JsonIndent.of(2);

    assertThat(indent)
        .as("of() should indent with spaces per level and start at column zero")
        .isEqualTo(new JsonIndent(2, 0, ' '));
    assertThat(indent.isCompact()).as("A positive factor should not be compact").isFalse();
  }

  @Test
  void aZeroFactorIsCompact() {
    assertThat(new JsonIndent(0, 4, ' ').isCompact())
        .as("Without a factor there is nothing to indent, whatever the initial indent says")
        .isTrue();
  }

  @Test
  void aNegativeFactorIsRejected() {
    assertThatThrownBy(() -> new JsonIndent(-1, 0, ' '))
        .as("A negative factor has no meaning")
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("factor");
  }

  @Test
  void aNegativeInitialIndentIsRejected() {
    assertThatThrownBy(() -> new JsonIndent(2, -1, ' '))
        .as("A negative initial indent has no meaning")
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("initial");
  }
}
