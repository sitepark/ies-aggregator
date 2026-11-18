package com.sitepark.ies.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SubSectionTypeTest {

  @Test
  void readsWhatTheContainerNeeds() {
    SubSectionType type =
        SubSectionType.of(
            "iframe2",
            null,
            Map.of("uri", "/aggregator/sectionTypes/iframe2.spml", "type", "iframe", "id", "map"),
            Map.of("subinformationFieldName", "sp_moreLikeThis"));

    assertThat(type)
        .isEqualTo(
            new SubSectionType("iframe2", true, null, "iframe", "map", null, "sp_moreLikeThis"));
  }

  @Test
  void componentTypeFallsBackToTheId() {
    SubSectionType type = SubSectionType.of("text", null, Map.of(), Map.of());

    assertThat(type.componentType())
        .as("without aggregator.type the component is typed by the section type's id")
        .isEqualTo("text");
  }

  @Test
  void onlyAnExplicitFalseSwitchesTheTypeOff() {
    assertThat(SubSectionType.of("text", false, Map.of(), Map.of()).enabledFor(Optional.empty()))
        .isFalse();
    assertThat(SubSectionType.of("text", "false", Map.of(), Map.of()).enabledFor(Optional.empty()))
        .isFalse();
    assertThat(SubSectionType.of("text", true, Map.of(), Map.of()).enabledFor(Optional.empty()))
        .isTrue();
    assertThat(SubSectionType.of("text", "", Map.of(), Map.of()).enabledFor(Optional.empty()))
        .as("`${_sectionType.enabled} == false` holds for false only")
        .isTrue();
  }

  @Test
  void naturesDecideOnlyWhereTheChannelDeclaresOne() {
    SubSectionType type =
        SubSectionType.of(
            "text",
            null,
            Map.of(
                "natures",
                Map.of("intranet", Map.of("enabled", true), "internet", Map.of("enabled", false))),
            Map.of());

    assertThat(type.enabledFor(Optional.empty()))
        .as("a channel without a nature does not ask")
        .isTrue();
    assertThat(type.enabledFor(Optional.of("intranet"))).isTrue();
    assertThat(type.enabledFor(Optional.of("internet"))).isFalse();
    assertThat(type.enabledFor(Optional.of("extranet")))
        .as("a nature the type does not name is off: its enabled is not true")
        .isFalse();
  }

  @Test
  void emptyNaturesSwitchTheTypeOffWhereTheChannelDeclaresOne() {
    SubSectionType type = SubSectionType.of("text", null, Map.of("natures", Map.of()), Map.of());

    assertThat(type.enabledFor(Optional.of("intranet")))
        .as("natures {} is not null, so the nature is asked for and is not enabled")
        .isFalse();
    assertThat(type.enabledFor(Optional.empty())).isTrue();
  }

  @Test
  void aTypeWithoutNaturesIsEnabledInEveryChannel() {
    SubSectionType type = SubSectionType.of("text", null, Map.of(), Map.of());

    assertThat(type.enabledFor(Optional.of("intranet"))).isTrue();
  }
}
