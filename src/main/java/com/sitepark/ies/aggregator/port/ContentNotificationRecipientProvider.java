package com.sitepark.ies.aggregator.port;

import com.sitepark.ies.aggregator.value.notification.ContentNotificationRecipients;
import java.util.Optional;

/**
 * Resolves who is notified about user-generated content.
 *
 * <p>Where visitors create, edit or delete content themselves, an editor names who is told about
 * it: by a message in the platform's inbox, by mail, or both. What the editor names is the
 * platform's business - a user group in one version, a role in another - so the aggregator hands
 * the stored reference over and gets back only what a notification needs.
 */
@FunctionalInterface
public interface ContentNotificationRecipientProvider {

  /**
   * Returns the recipients the given reference names.
   *
   * @param reference what the editor stored to name the recipients, e.g. the anchor of a user group
   * @return the recipients, or {@link Optional#empty()} if the reference names nothing the platform
   *     can notify
   */
  Optional<ContentNotificationRecipients> recipients(String reference);
}
