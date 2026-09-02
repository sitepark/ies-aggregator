package com.sitepark.ies.aggregator.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmptyUserTest {

  @Test
  void idIsEmpty() {
    assertThat(User.empty().id()).as("An empty user should have an empty id").isEmpty();
  }

  @Test
  void nameIsEmpty() {
    assertThat(User.empty().name()).as("An empty user should have an empty name").isEmpty();
  }

  @Test
  void anchorIsEmpty() {
    assertThat(User.empty().anchor()).as("An empty user should have an empty anchor").isEmpty();
  }

  @Test
  void firstNameIsEmpty() {
    assertThat(User.empty().firstName())
        .as("An empty user should have an empty given name")
        .isEmpty();
  }

  @Test
  void lastNameIsEmpty() {
    assertThat(User.empty().lastName())
        .as("An empty user should have an empty family name")
        .isEmpty();
  }

  @Test
  void emptyIsAlwaysTheSameInstance() {
    assertThat(User.empty())
        .as("The empty user is stateless and should be shared")
        .isSameAs(User.empty());
  }
}
