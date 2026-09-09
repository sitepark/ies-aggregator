package com.sitepark.ies.aggregator.value;

import com.sitepark.ies.aggregator.value.uri.Uri;

/**
 * The published file a resource becomes: what kind of artefact it is, where it is written and under
 * which address it is reachable.
 *
 * <p>Distinct from the {@link com.sitepark.ies.aggregator.port.Channel Channel}, which describes the
 * publication target as a whole. This describes one file in it, and it is what an {@link
 * com.sitepark.ies.aggregator.output.format.OutputWriter OutputWriter} needs to know beside the
 * aggregated tree: the {@link #type()} says what role the file plays, the {@link #resourcePath()}
 * how deep in the tree it sits — a writer that has to reference a sibling file computes the way back
 * from it — and the {@link #uri()} under which address a consumer reaches it.
 *
 * @param id the id of the object being published
 * @param type what role the published file plays
 * @param resourcePath the path the file is written to, starting with {@code /}
 * @param uri the address the published file is reachable under
 */
public record Publication(int id, PublicationType type, String resourcePath, Uri uri) {}
