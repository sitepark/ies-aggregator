package com.sitepark.ies.aggregator.value.media.scaling;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class SourceImageTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(SourceImage.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(SourceImage.class).verify();
  }
}
