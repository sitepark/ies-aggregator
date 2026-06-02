package com.sitepark.ies.aggregator.value;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class PlainUriTest {

  @Test
  void testEquals() {
    EqualsVerifier.forClass(PlainUri.class).withNonnullFields("originUri").verify();
  }

  @Test
  void allComponentsAreExposedFromWrappedUri() {
    PlainUri uri = PlainUri.of("https://user@example.com:8080/foo/bar?q=1#top");

    assertThat(uri.scheme()).as("scheme should be exposed").isEqualTo("https");
    assertThat(uri.userInfo()).as("userInfo should be exposed").isEqualTo("user");
    assertThat(uri.host()).as("host should be exposed").isEqualTo("example.com");
    assertThat(uri.port()).as("port should be exposed").isEqualTo(8080);
    assertThat(uri.path()).as("path should be exposed").isEqualTo("/foo/bar");
    assertThat(uri.query()).as("query should be exposed").isEqualTo("q=1");
    assertThat(uri.fragment()).as("fragment should be exposed").isEqualTo("top");
  }

  @Test
  void absentUserInfoIsNull() {
    assertThat(PlainUri.of("https://example.com/foo").userInfo())
        .as("userInfo should be null when not present in the URI")
        .isNull();
  }

  @Test
  void absentPortIsMinusOne() {
    assertThat(PlainUri.of("https://example.com/foo").port())
        .as("port should be -1 when not present in the URI")
        .isEqualTo(-1);
  }

  @Test
  void absentQueryIsNull() {
    assertThat(PlainUri.of("https://example.com/foo").query())
        .as("query should be null when not present in the URI")
        .isNull();
  }

  @Test
  void absentFragmentIsNull() {
    assertThat(PlainUri.of("https://example.com/foo").fragment())
        .as("fragment should be null when not present in the URI")
        .isNull();
  }

  @Test
  void toStringReturnsOriginUri() {
    String s = "https://example.com/foo?q=1";
    assertThat(PlainUri.of(s).toString())
        .as("toString() should return the wrapped URI string verbatim")
        .isEqualTo(s);
  }

  @Test
  void invalidStringIsRejected() {
    assertThatThrownBy(() -> PlainUri.of("ht tp://bad uri"))
        .as("of(String) should propagate URISyntax errors as IllegalArgumentException")
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void updatePathReplacesOnlyThePathComponent() {
    PlainUri original = PlainUri.of("https://example.com/old?q=1#top");

    PlainUri updated = PlainUri.updatePath(original, "/new");

    assertThat(updated.toString())
        .as("updatePath should preserve scheme, host, query and fragment, replacing only the path")
        .isEqualTo("https://example.com/new?q=1#top");
  }

  @Test
  void updatePathPreservesUserInfoAndPort() {
    PlainUri original = PlainUri.of("https://user@example.com:8080/old");

    PlainUri updated = PlainUri.updatePath(original, "/new");

    assertThat(updated.toString())
        .as("updatePath should preserve userInfo and port components")
        .isEqualTo("https://user@example.com:8080/new");
  }

  @Test
  void updatePathDoesNotMutateOriginal() {
    PlainUri original = PlainUri.of("https://example.com/old");

    PlainUri.updatePath(original, "/new");

    assertThat(original.path()).as("updatePath must not mutate the original Uri").isEqualTo("/old");
  }

  @Test
  void ofUriDelegatesToConstructor() {
    PlainUri uri = PlainUri.of(URI.create("https://example.com/foo"));

    assertThat(uri.toString())
        .as("of(URI) should wrap the URI verbatim")
        .isEqualTo("https://example.com/foo");
  }

  @Test
  void ofUrlConvertsViaToUri() throws Exception {
    PlainUri uri = PlainUri.of(URI.create("https://example.com/foo").toURL());

    assertThat(uri.toString())
        .as("of(URL) should wrap the URL converted to a URI")
        .isEqualTo("https://example.com/foo");
  }

  @Test
  void absoluteUriReportsAbsolute() {
    assertThat(PlainUri.of("https://example.com/foo").isAbsolute())
        .as("a URI with a scheme should report itself as absolute")
        .isTrue();
  }

  @Test
  void relativeUriReportsNotAbsolute() {
    assertThat(PlainUri.of("/foo/bar").isAbsolute())
        .as("a URI without a scheme should report itself as not absolute")
        .isFalse();
  }

  @Test
  void authorityIsExposed() {
    assertThat(PlainUri.of("https://user@example.com:8080/foo").authority())
        .as("authority should combine userInfo, host and port")
        .isEqualTo("user@example.com:8080");
  }

  @Test
  void updatePathRejectsInvalidResultingUri() {
    PlainUri original = PlainUri.of("https://example.com/old");

    assertThatThrownBy(() -> PlainUri.updatePath(original, "relative-without-leading-slash"))
        .as("updatePath should reject a path that yields an invalid URI for a URI with authority")
        .isInstanceOf(IllegalArgumentException.class);
  }

  // --- toAbsolutePathReference --------------------------------------------

  @Test
  void toAbsolutePathReferenceStripsSchemeAndAuthority() {
    PlainUri uri = PlainUri.of("https://example.com/foo/bar?x=1#section");

    assertThat(uri.toAbsolutePathReference().toString())
        .as("toAbsolutePathReference should keep only path, query and fragment")
        .isEqualTo("/foo/bar?x=1#section");
  }

  @Test
  void toAbsolutePathReferenceRejectsNonAbsolutePath() {
    PlainUri uri = PlainUri.of("relative/path");

    assertThatThrownBy(uri::toAbsolutePathReference)
        .as("toAbsolutePathReference should reject a path that is not absolute")
        .isInstanceOf(IllegalArgumentException.class);
  }

  // --- toAbsoluteUri ------------------------------------------------------

  @Test
  void toAbsoluteUriCombinesPathReferenceWithBaseAuthority() {
    PlainUri reference =
        PlainUri.of("https://other.example/foo/bar?x=1#section").toAbsolutePathReference();
    PlainUri base = PlainUri.of("https://example.com/ignored/path");

    assertThat(reference.toAbsoluteUri(base).toString())
        .as("toAbsoluteUri should take scheme and authority from base, keeping path/query/fragment")
        .isEqualTo("https://example.com/foo/bar?x=1#section");
  }

  @Test
  void toAbsoluteUriRejectsNonAbsolutePathReference() {
    PlainUri reference = PlainUri.of("relative/path");
    PlainUri base = PlainUri.of("https://example.com/");

    assertThatThrownBy(() -> reference.toAbsoluteUri(base))
        .as("toAbsoluteUri should reject a reference that is not an absolute-path reference")
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void toAbsoluteUriRejectsNonAbsoluteBase() {
    PlainUri reference = PlainUri.of("/foo");
    PlainUri base = PlainUri.of("/also-relative");

    assertThatThrownBy(() -> reference.toAbsoluteUri(base))
        .as("toAbsoluteUri should reject a base URI that is not absolute")
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void toAbsoluteUriRejectsBaseWithoutAuthority() {
    PlainUri reference = PlainUri.of("/foo");
    PlainUri base = PlainUri.of("mailto:someone@example.com");

    assertThatThrownBy(() -> reference.toAbsoluteUri(base))
        .as("toAbsoluteUri should reject an absolute base URI that has no authority")
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void translatableWrapsThisUri() {
    PlainUri uri = PlainUri.of("https://example.com/foo");

    assertThat(uri.translatable())
        .as("translatable() should return a TranslatableUri wrapping this URI")
        .isNotNull();
  }
}
