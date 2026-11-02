package com.sitepark.ies.aggregator.value.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ContentNotificationRecipientsTest {

  @Test
  void keepsTheAddressesItWasBuiltWith() {
    List<String> addresses = new ArrayList<>(List.of("editor@example.org"));
    ContentNotificationRecipients recipients =
        new ContentNotificationRecipients("100560100000001036-4000", addresses);

    addresses.add("late@example.org");

    assertThat(recipients.emailAddresses())
        .as("the record copies the addresses, so a later change of the list does not reach it")
        .containsExactly("editor@example.org");
  }
}
