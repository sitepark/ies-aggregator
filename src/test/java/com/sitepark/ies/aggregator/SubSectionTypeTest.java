package com.sitepark.ies.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class SubSectionTypeTest {

  @Test
  void readsWhatTheContainerNeeds() {
    SubSectionType type =
        SubSectionType.of(
            "iframe2",
            Map.of("uri", "/aggregator/sectionTypes/iframe2.spml", "type", "iframe", "id", "map"),
            Map.of("subinformationFieldName", "sp_moreLikeThis"));

    assertThat(type).isEqualTo(new SubSectionType("iframe2", "iframe", "map", "sp_moreLikeThis"));
  }

  @Test
  void readsTheNodeTheTypeIsPlacedIn() {
    SubSectionType type =
        SubSectionType.of(
            "errorPage", Map.of("content", Map.of("type", "main", "id", "main")), Map.of());

    assertThat(type.content())
        .as("aggregator.content, as sectionType.spml resolves it")
        .isEqualTo(new SubSectionType.Content("main", "main"));
  }

  @Test
  void aContentWithoutTypeIsNone() {
    assertThat(SubSectionType.of("text", Map.of("content", Map.of("id", "x")), Map.of()).content())
        .as("sectionType.spml asks for aggregator.content.type")
        .isNull();
  }

  @Test
  void componentTypeFallsBackToTheId() {
    SubSectionType type = SubSectionType.of("text", Map.of(), Map.of());

    assertThat(type.componentType())
        .as("without aggregator.type the component is typed by the section type's id")
        .isEqualTo("text");
  }

  @Test
  void anEmptyValueIsNone() {
    SubSectionType type = SubSectionType.of("text", Map.of("type", ""), Map.of());

    assertThat(type.componentType())
        .as("an empty aggregator.type is no type, as an empty value is nothing in SPML")
        .isEqualTo("text");
  }
}
