package com.sitepark.ies.aggregator.value.notification;

import java.util.List;

/**
 * Who is notified about user-generated content.
 *
 * @param inboxRecipient the participant a message in the platform's inbox is addressed to, as the
 *     platform names it (today the qualified id of a user group); never empty
 * @param emailAddresses the addresses a notification mail is sent to, in the platform's order; empty if the
 *     recipients have none
 */
public record ContentNotificationRecipients(String inboxRecipient, List<String> emailAddresses) {

  /** Copies the addresses, so the record cannot change after it was built. */
  public ContentNotificationRecipients {
    emailAddresses = List.copyOf(emailAddresses);
  }
}
