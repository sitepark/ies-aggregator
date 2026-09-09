package com.sitepark.ies.aggregator.output.format;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sitepark.ies.aggregator.output.DomainObjectMapper;
import com.sitepark.ies.aggregator.output.OutputObject;
import com.sitepark.ies.aggregator.port.Channel;
import com.sitepark.ies.aggregator.value.Publication;
import com.sitepark.ies.aggregator.value.PublicationType;
import com.sitepark.ies.aggregator.value.ResourcePathType;
import com.sitepark.ies.aggregator.value.text.Translations;
import com.sitepark.ies.aggregator.value.uri.Uri;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.junit.jupiter.api.Test;

/**
 * Verifies that {@link OutputContext} carries what a document writer needs, using an example
 * implementation.
 *
 * <p>Whether the interface is sufficient is not provable by inspection — only by writing a document
 * with it. The example below is deliberately the hard case in miniature: it picks one of two frames
 * by what the channel says, computes a relative path from what the publication says, and renders two
 * areas through the value layer. It lives in the test source tree because the port contributes no
 * format of its own.
 */
class OutputWriterTest {

  /**
   * An example writer: either a bare data literal or a script that resolves its own context, chosen
   * by how the channel addresses its resources.
   */
  // A value writer per area is the point, not an oversight: each area is an independently indented
  // block, and a fresh instance is what starts its indentation at zero.
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private static final class ExampleWriter implements OutputWriter {

    @Override
    public void write(OutputObject root, OutputContext context, Writer target) throws IOException {
      target.write("<?php\n");
      if (context.channel().resourcePathType() == ResourcePathType.URL) {
        target.write(
            "include('" + wayBack(context.publication().resourcePath()) + "bootstrap');\n");
      }
      target.write("return [\n\t'type' => '" + context.publication().type() + "',\n");
      for (String area : new String[] {"init", "base"}) {
        target.write("\t'" + area + "' => ");
        // A fresh value-layer writer per area: its indentation starts at zero, which is what an
        // independently indented block needs.
        new PhpArrayWriter(target, context.domainObjectMapper(), context.translations())
            .visitObject(root.node(area));
        target.write(",\n");
      }
      target.write("];\n");
    }

    /** The way from the file back to the document root: one {@code ../} per path segment above it. */
    private static String wayBack(String resourcePath) {
      String[] segments = resourcePath.split("/");
      StringBuilder path = new StringBuilder();
      for (int i = 1; i < segments.length - 1; i++) {
        path.append("../");
      }
      return path.length() == 0 ? "./" : path.toString();
    }
  }

  private static OutputContext context(ResourcePathType pathType, String resourcePath) {
    Channel channel = mock(Channel.class);
    when(channel.resourcePathType()).thenReturn(pathType);
    Publication publication =
        new Publication(
            42, PublicationType.OBJECT, resourcePath, Uri.of("https://example.com/page"));
    return new OutputContext(DomainObjectMapper.NONE, Translations.SOURCE, channel, publication);
  }

  private static String write(ResourcePathType pathType, String resourcePath) throws IOException {
    OutputObject root = new OutputObject(null, null);
    root.node("init").put("id", 42);
    root.node("base").put("title", "Page");

    StringWriter target = new StringWriter();
    new ExampleWriter().write(root, context(pathType, resourcePath), target);
    return target.toString();
  }

  @Test
  void writesTheFrameTheChannelCallsFor() throws IOException {
    assertThat(write(ResourcePathType.URL, "/content/page"))
        .as("a resource addressed by its path has to resolve its own context")
        .contains("include(");

    assertThat(write(ResourcePathType.ID, "/content/page"))
        .as("a resource addressed by its id can be plain data")
        .doesNotContain("include(");
  }

  @Test
  void computesTheWayBackFromThePublicationPath() throws IOException {
    assertThat(write(ResourcePathType.URL, "/page"))
        .as("a file at the document root reaches it without going up")
        .contains("include('./bootstrap')");

    assertThat(write(ResourcePathType.URL, "/a/b/page"))
        .as("a file two levels down goes up twice")
        .contains("include('../../bootstrap')");
  }

  @Test
  void rendersEachAreaThroughTheValueLayer() throws IOException {
    assertThat(write(ResourcePathType.ID, "/page"))
        .as("both areas are rendered, each starting its own indentation")
        .contains("'init' => [\n\t\"id\" => 42\n]")
        .contains("'base' => [\n\t\"title\" => \"Page\"\n]");
  }

  @Test
  void readsThePublicationType() throws IOException {
    assertThat(write(ResourcePathType.ID, "/page"))
        .as("the role of the published file reaches the writer")
        .contains("'type' => 'object'");
  }
}
