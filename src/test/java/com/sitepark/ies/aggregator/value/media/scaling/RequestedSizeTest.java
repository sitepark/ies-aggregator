package com.sitepark.ies.aggregator.value.media.scaling;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class RequestedSizeTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(RequestedSize.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(RequestedSize.class).verify();
  }
}
