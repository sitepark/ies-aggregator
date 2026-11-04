package com.sitepark.ies.aggregator.port;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class ChannelTest {

  @Test
  void aChannelThatDoesNotKnowItsUrlAnswersNone() {
    Channel channel = mock(Channel.class, CALLS_REAL_METHODS);

    assertThat(channel.url()).as("the default never invents a host").isEmpty();
  }
}
