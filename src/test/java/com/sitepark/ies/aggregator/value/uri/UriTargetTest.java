package com.sitepark.ies.aggregator.value.uri;

import static org.assertj.core.api.Assertions.assertThat;

import com.sitepark.ies.aggregator.value.uri.UriTarget.MediaTarget;
import com.sitepark.ies.aggregator.value.uri.UriTarget.ObjectTarget;
import org.junit.jupiter.api.Test;

class UriTargetTest {

  @Test
  void ofObjectCreatesObjectTargetWithGivenId() {
    UriTarget target = UriTarget.ofObject(42);

    assertThat(target)
        .as("ofObject() should create an ObjectTarget carrying the given object id")
        .isEqualTo(new ObjectTarget(42));
  }

  @Test
  void ofEmbeddedMediaCreatesMediaTargetWithObjectAndMediaId() {
    UriTarget target = UriTarget.ofMedia(42, 7);

    assertThat(target)
        .as("ofEmbeddedMedia() should create an EmbeddedMediaTarget carrying object and media id")
        .isEqualTo(new MediaTarget(42, 7));
  }
}
