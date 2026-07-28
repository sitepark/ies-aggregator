package com.sitepark.ies.aggregator.value.media.scaling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.sitepark.ies.aggregator.value.media.*;
import com.sitepark.ies.aggregator.value.uri.UriTarget;
import java.util.ArrayList;
import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ScaleImageRequestTest {

  private static ScaleImageTarget aTarget() {
    return ScaleImageTarget.of(ComputedSize.of(800, 600, Padding.NO, 800, 600), "FFFFFF");
  }

  @Test
  void testEquals() {
    EqualsVerifier.forClass(ScaleImageRequest.class)
        .withPrefabValues(UriTarget.class, UriTarget.ofObject(1), UriTarget.ofObject(2))
        .verify();
  }

  @Test
  void exposesSourceFocalPointAndTargets() {
    Image image = mock();

    ScaleImageRequest request = ScaleImageRequest.of(image, List.of(aTarget()));

    assertThat(request.image())
        .as("source() should return the constructor argument")
        .isSameAs(image);
    assertThat(request.targets())
        .as("targets() should return the requested targets")
        .containsExactly(aTarget());
  }

  @Test
  void copiesTargetsDefensively() {
    List<ScaleImageTarget> targets = new ArrayList<>(List.of(aTarget()));
    ScaleImageRequest request = ScaleImageRequest.of(mock(), targets);

    targets.clear();

    assertThat(request.targets())
        .as("targets should be copied defensively so later mutation of the input has no effect")
        .hasSize(1);
  }
}
