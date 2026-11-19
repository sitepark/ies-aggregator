package com.sitepark.ies.aggregator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class StepTest {

  private static PlacedSectionType text() {
    return new PlacedSectionType(new SubSectionType("text", null, null, null), null);
  }

  @Test
  void keepsTheSectionTypesAsHandedIn() {
    List<PlacedSectionType> handedIn = new ArrayList<>(List.of(text()));
    Step step = new Step("content", handedIn);
    handedIn.clear();

    assertThat(step.sectionTypes())
        .as("a later change of the caller's list does not reach the step")
        .hasSize(1);
  }

  @Test
  void isImmutable() {
    Step step = new Step("content", List.of(text()));

    assertThatThrownBy(() -> step.sectionTypes().add(text()))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
