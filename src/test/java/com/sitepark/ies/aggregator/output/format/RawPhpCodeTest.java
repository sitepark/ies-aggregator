package com.sitepark.ies.aggregator.output.format;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class RawPhpCodeTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(RawPhpCode.class).withNonnullFields("code").verify();
  }

  @Test
  void constructorRejectsNullCode() {
    assertThatThrownBy(() -> new RawPhpCode(null))
        .as("Constructor should reject null code with NullPointerException")
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void codeReturnsRawString() {
    assertThat(new RawPhpCode("foo()").code())
        .as("code() should return the raw PHP code string")
        .isEqualTo("foo()");
  }

  @Test
  void toStringReturnsRawString() {
    assertThat(new RawPhpCode("foo()").toString())
        .as("toString() should return the raw PHP code string verbatim")
        .isEqualTo("foo()");
  }
}
