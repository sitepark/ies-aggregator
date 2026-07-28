package com.sitepark.ies.aggregator.output.format;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CodeTest {

  @Test
  void ofCreatesPlainCode() {
    assertThat(Code.of("foo"))
        .as("Code.of() should create a PlainCode carrying the given content")
        .isEqualTo(PlainCode.of("foo"));
  }

  @Test
  void phpCreatesRawPhpCode() {
    assertThat(Code.php("foo()"))
        .as("Code.php() should create a RawPhpCode carrying the given code")
        .isEqualTo(new RawPhpCode("foo()"));
  }

  @Test
  void emptyIsEmptyPlainCode() {
    assertThat(Code.empty().isEmpty()).as("Code.empty() should return an empty PlainCode").isTrue();
  }
}
