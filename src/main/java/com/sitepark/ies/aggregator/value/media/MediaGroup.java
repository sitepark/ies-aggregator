package com.sitepark.ies.aggregator.value.media;

/**
 * Settings of the media group (the container/folder a {@link Media} asset lives in) that affect how
 * the contained media is presented.
 *
 * @param ignoreDescription whether the description of contained media should be ignored
 * @param ignoreAlternativeText whether the alternative text of contained media should be ignored
 * @param ignoreCopyright whether the copyright of contained media should be ignored
 * @param ignoreTitle whether the title of contained media should be ignored
 */
public record MediaGroup(
    boolean ignoreDescription,
    boolean ignoreAlternativeText,
    boolean ignoreCopyright,
    boolean ignoreTitle) {}
