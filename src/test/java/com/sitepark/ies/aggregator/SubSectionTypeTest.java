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
