package com.sitepark.ies.aggregator.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmptyGroupDescriptorTest {

  @Test
  void idIsZero() {
    assertThat(GroupDescriptor.empty().id()).as("An empty group should have id 0").isZero();
  }

  @Test
  void qualifiedIdIsEmpty() {
    assertThat(GroupDescriptor.empty().qualifiedId())
        .as("An empty group should have an empty qualified id")
        .isEmpty();
  }

  @Test
  void versionIsEmpty() {
    assertThat(GroupDescriptor.empty().version())
        .as("An empty group should have an empty version")
        .isEmpty();
  }

  @Test
  void typeIsEmpty() {
    assertThat(GroupDescriptor.empty().type())
        .as("An empty group should have an empty type")
        .isEmpty();
  }

  @Test
  void nameIsEmpty() {
    assertThat(GroupDescriptor.empty().name())
        .as("An empty group should have an empty name")
        .isEmpty();
  }

  @Test
  void anchorIsEmpty() {
    assertThat(GroupDescriptor.empty().anchor())
        .as("An empty group should have an empty anchor")
        .isEmpty();
  }

  @Test
  void filenameIsEmpty() {
    assertThat(GroupDescriptor.empty().filename())
        .as("An empty group should have an empty file name")
        .isEmpty();
  }

  @Test
  void createdIsTheEmptyRevision() {
    assertThat(GroupDescriptor.empty().created())
        .as("An empty group was never created and should expose the empty revision")
        .isSameAs(Revision.empty());
  }

  @Test
  void changedIsTheEmptyRevision() {
    assertThat(GroupDescriptor.empty().changed())
        .as("An empty group was never changed and should expose the empty revision")
        .isSameAs(Revision.empty());
  }

  @Test
  void isNotASiteRoot() {
    assertThat(GroupDescriptor.empty().isRootSite())
        .as("An empty group should not be a site root")
        .isFalse();
  }

  @Test
  void isNotAMicrositeRoot() {
    assertThat(GroupDescriptor.empty().isMicrositeRootSite())
        .as("An empty group should not be a microsite root")
        .isFalse();
  }

  @Test
  void langIsEmpty() {
    assertThat(GroupDescriptor.empty().lang())
        .as("An empty group should have an empty language")
        .isEmpty();
  }

  @Test
  void emptyIsAlwaysTheSameInstance() {
    assertThat(GroupDescriptor.empty())
        .as("The empty group descriptor is stateless and should be shared")
        .isSameAs(GroupDescriptor.empty());
  }
}
