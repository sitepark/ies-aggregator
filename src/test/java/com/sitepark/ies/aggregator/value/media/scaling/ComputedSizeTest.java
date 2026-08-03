package com.sitepark.ies.aggregator.value.media.scaling;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ComputedSizeTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(ComputedSize.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(ComputedSize.class).verify();
  }
}
