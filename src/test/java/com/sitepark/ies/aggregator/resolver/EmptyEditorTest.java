package com.sitepark.ies.aggregator.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmptyEditorTest {

  @Test
  void idIsEmpty() {
    assertThat(Editor.empty().id()).as("An empty editor should have an empty id").isEmpty();
  }

  @Test
  void nameIsEmpty() {
    assertThat(Editor.empty().name()).as("An empty editor should have an empty name").isEmpty();
  }

  @Test
  void anchorIsEmpty() {
    assertThat(Editor.empty().anchor()).as("An empty editor should have an empty anchor").isEmpty();
  }

  @Test
  void firstNameIsEmpty() {
    assertThat(Editor.empty().firstName())
        .as("An empty editor should have an empty given name")
        .isEmpty();
  }

  @Test
  void lastNameIsEmpty() {
    assertThat(Editor.empty().lastName())
        .as("An empty editor should have an empty family name")
        .isEmpty();
  }

  @Test
  void emptyIsAlwaysTheSameInstance() {
    assertThat(Editor.empty())
        .as("The empty editor is stateless and should be shared")
        .isSameAs(Editor.empty());
  }
}
