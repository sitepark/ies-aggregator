package com.sitepark.ies.aggregator.output.format;

import com.sitepark.ies.aggregator.output.DomainObjectMapper;
import com.sitepark.ies.aggregator.port.Channel;
import com.sitepark.ies.aggregator.value.Publication;
import com.sitepark.ies.aggregator.value.text.Translations;

/**
 * What an {@link OutputWriter} needs beside the output tree.
 *
 * <p>Everything a writer decides by — which frame to write, whether a translation is being rendered,
 * how a domain model unwraps — is passed in here rather than looked up. That keeps a writer
 * stateless and usable outside the aggregation that produced the tree: by the time a document is
 * written, the aggregation has ended.
 *
 * <p>A parameter object, not a value: it carries the collaborators a writer works with, so it has no
 * meaningful identity of its own. Bundling them keeps the {@link OutputWriter#write} signature
 * stable — a further fact a writer turns out to need is added here, not to every implementation.
 *
 * @param domainObjectMapper unwraps the domain models the aggregators placed in the tree
 * @param translations the table applied while rendering, and — via {@link
 *     Translations#targetLang()} — whether a translation is being rendered at all; {@link
 *     Translations#SOURCE} renders the source language
 * @param channel the channel being published into, which answers how it addresses its resources
 *     (see {@link Channel#resourcePathType()})
 * @param publication the file being written: its role, its place and its address
 */
public record OutputContext(
    DomainObjectMapper domainObjectMapper,
    Translations translations,
    Channel channel,
    Publication publication) {}
