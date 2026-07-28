package com.sitepark.ies.aggregator.value.media.scaling;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ScaleImageTargetTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(ScaleImageTarget.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(ScaleImageTarget.class).verify();
  }
}
