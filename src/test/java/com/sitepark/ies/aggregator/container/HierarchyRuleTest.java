package com.sitepark.ies.aggregator.container;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HierarchyRuleTest {

  @Test
  void readsEveryKeyOfTheAggregatorConfiguration() {
    HierarchyRule rule =
        HierarchyRule.of(
            Map.of(
                "newSection",
                true,
                "sectionLevel",
                2,
                "newSectionType",
                "cards",
                "newSectionAfter",
                true,
                "newSectionAfterLevel",
                1,
                "implicitSection",
                true,
                "type",
                "stuttgart.collapsible"));

    assertThat(rule)
        .isEqualTo(new HierarchyRule(true, 2, "cards", true, 1, true, "stuttgart.collapsible"));
  }

  @Test
  void anEmptyConfigurationOnlyAddsContent() {
    assertThat(HierarchyRule.of(Map.of()))
        .as("no key set: the entry neither opens a section nor carries a type")
        .isEqualTo(HierarchyRule.CONTENT);
  }

  @Test
  void blanksAndForeignValuesCountAsUnset() {
    HierarchyRule rule =
        HierarchyRule.of(
            Map.of("newSection", "true", "sectionLevel", "2", "newSectionType", " ", "type", ""));

    assertThat(rule)
        .as("only a boolean true opens a section, only a number is a level, only text a type")
        .isEqualTo(HierarchyRule.CONTENT);
  }

  @Test
  void anOpenedSectionIsTypedLikeTheFormerAggregator() {
    assertThat(
            List.of(
                new HierarchyRule(true, null, "cards", false, null, false, "text").outputType(),
                new HierarchyRule(true, null, null, false, null, false, "text").outputType(),
                new HierarchyRule(true, null, null, false, null, false).outputType()))
        .as("newSectionType, else the entry's output type, else section")
        .containsExactly("cards", "text", HierarchyRule.DEFAULT_SECTION_TYPE);
  }

  @Test
  void theEntryIsTheSectionOnlyWithoutAWrapper() {
    assertThat(new HierarchyRule(true, null, null, false, null, false).itemIsSection()).isTrue();
    assertThat(new HierarchyRule(true, null, "cards", false, null, false).itemIsSection())
        .isFalse();
    assertThat(HierarchyRule.CONTENT.itemIsSection()).isFalse();
  }
}
