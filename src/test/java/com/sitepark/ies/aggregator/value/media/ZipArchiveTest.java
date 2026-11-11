package com.sitepark.ies.aggregator.value.media;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ZipArchiveTest {

  @Test
  void keepsLettersDigitsDotsAndHyphens() {
    assertThat(ZipArchive.filenameOf("Fotos-2026.v1"))
        .as("the characters sp:zip keeps stay")
        .isEqualTo("Fotos-2026.v1.zip");
  }

  @Test
  void replacesEveryOtherCharacterWithAnUnderscore() {
    assertThat(ZipArchive.filenameOf("Rathaus Süd/Bilder (neu)"))
        .as("blank, umlaut, slash and parentheses each become one underscore")
        .isEqualTo("Rathaus_S_d_Bilder__neu_.zip");
  }

  @Test
  void anEmptyNameYieldsTheBareSuffix() {
    assertThat(ZipArchive.filenameOf(""))
        .as("sp:zip appends .zip to whatever the name leaves")
        .isEqualTo(".zip");
  }
}
