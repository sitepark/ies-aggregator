package com.sitepark.ies.aggregator.value.media;

/**
 * Where a media asset came from: the external asset-management system it was synchronised from, and
 * the id it carries there. Together the two address the asset in that foreign system.
 *
 * @param system the key of the source system (e.g. {@code pixxio}, {@code panbase})
 * @param id the id of the asset within that system
 */
public record Origin(String system, String id) {}
