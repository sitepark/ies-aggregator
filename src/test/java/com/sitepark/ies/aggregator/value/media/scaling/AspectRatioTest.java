package com.sitepark.ies.aggregator.value.media.scaling;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class AspectRatioTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(AspectRatio.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(AspectRatio.class).verify();
  }
}
