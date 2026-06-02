package com.sitepark.ies.aggregator.value;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ChannelTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(Channel.class).verify();
  }

  @Test
  void testToString() {
    ToStringVerifier.forClass(Channel.class).verify();
  }
}
