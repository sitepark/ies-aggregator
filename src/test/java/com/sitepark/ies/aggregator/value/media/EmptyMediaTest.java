package com.sitepark.ies.aggregator.value.media;

import static org.assertj.core.api.Assertions.assertThat;

import com.sitepark.ies.aggregator.value.Emptiable;
import org.junit.jupiter.api.Test;

class EmptyMediaTest {

  private static Image image() {
    return new Image(
        42,
        7,
        "logo.png",
        "Logo.png",
        "image/png",
        1024L,
        new Hash(HashAlgorithm.SHA_256, "abc"),
        new ImageMetadata(null, null, null, null, null, FocalPoint.CENTER),
        800,
        600,
        null);
  }

  @Test
  void emptyReturnsSingleInstance() {
    assertThat(Media.empty())
        .as("Media.empty() should always return the same instance")
        .isSameAs(Media.empty());
  }

  @Test
  void emptyMediaIsEmpty() {
    assertThat(Media.empty().isEmpty()).as("Media.empty() should report empty").isTrue();
  }

  @Test
  void concreteMediaKindIsNotEmpty() {
    Media media = image();

    assertThat(media.isEmpty()).as("a concrete media kind should never report empty").isFalse();
  }

  @Test
  void emptyMediaIsRecognizedByEmptiable() {
    assertThat(Emptiable.isEmpty(Media.empty()))
        .as("Emptiable.isEmpty() should recognize the empty media asset")
        .isTrue();
  }

  @Test
  void emptyMediaHasNeutralIds() {
    Media media = Media.empty();

    assertThat(media.objectId()).as("empty media should have object id 0").isZero();
    assertThat(media.id()).as("empty media should have id 0").isZero();
  }

  @Test
  void emptyMediaHasNeutralFileData() {
    Media media = Media.empty();

    assertThat(media.filename()).as("empty media should have an empty filename").isEmpty();
    assertThat(media.originFilename())
        .as("empty media should have an empty origin filename")
        .isEmpty();
    assertThat(media.mimeType()).as("empty media should have an empty mime type").isEmpty();
    assertThat(media.fileSize()).as("empty media should have file size 0").isZero();
  }

  @Test
  void emptyMediaHasEmptyHash() {
    assertThat(Media.empty().hash().value())
        .as("empty media should have a hash without a digest value")
        .isEmpty();
  }

  @Test
  void emptyMediaHasMetadataWithoutDescriptiveData() {
    Metadata metadata = Media.empty().metadata();

    assertThat(metadata.alternativeText())
        .as("empty media metadata should have no alternative text")
        .isNull();
    assertThat(metadata.copyright()).as("empty media metadata should have no copyright").isNull();
    assertThat(metadata.title()).as("empty media metadata should have no title").isNull();
    assertThat(metadata.description())
        .as("empty media metadata should have no description")
        .isNull();
    assertThat(metadata.lastModified())
        .as("empty media metadata should have no modification timestamp")
        .isNull();
  }

  @Test
  void testToString() {
    assertThat(Media.empty().toString())
        .as("toString() should name the empty media asset")
        .isEqualTo("EmptyMedia");
  }
}
