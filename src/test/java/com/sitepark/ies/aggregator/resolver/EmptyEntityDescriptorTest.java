package com.sitepark.ies.aggregator.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmptyEntityDescriptorTest {

  @Test
  void idIsZero() {
    assertThat(EntityDescriptor.empty().id()).as("An empty entity should have id 0").isZero();
  }

  @Test
  void typeIsEmpty() {
    assertThat(EntityDescriptor.empty().type())
        .as("An empty entity should have an empty type")
        .isEmpty();
  }

  @Test
  void nameIsEmpty() {
    assertThat(EntityDescriptor.empty().name())
        .as("An empty entity should have an empty name")
        .isEmpty();
  }

  @Test
  void anchorIsEmpty() {
    assertThat(EntityDescriptor.empty().anchor())
        .as("An empty entity should have an empty anchor")
        .isEmpty();
  }

  @Test
  void createdIsTheEmptyRevision() {
    assertThat(EntityDescriptor.empty().created())
        .as("An empty entity was never created and should expose the empty revision")
        .isSameAs(Revision.empty());
  }

  @Test
  void changedIsTheEmptyRevision() {
    assertThat(EntityDescriptor.empty().changed())
        .as("An empty entity was never changed and should expose the empty revision")
        .isSameAs(Revision.empty());
  }

  @Test
  void emptyIsAlwaysTheSameInstance() {
    assertThat(EntityDescriptor.empty())
        .as("The empty descriptor is stateless and should be shared")
        .isSameAs(EntityDescriptor.empty());
  }
}
