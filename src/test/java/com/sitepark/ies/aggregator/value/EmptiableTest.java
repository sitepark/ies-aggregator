package com.sitepark.ies.aggregator.value;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EmptiableTest {

  @Test
  void nullIsEmpty() {
    assertThat(Emptiable.isEmpty(null)).as("null should be treated as empty").isTrue();
  }

  @Test
  void emptiableReportingEmptyIsEmpty() {
    assertThat(Emptiable.isEmpty((Emptiable) () -> true))
        .as("An Emptiable that reports empty should be empty")
        .isTrue();
  }

  @Test
  void emptiableReportingNonEmptyIsNotEmpty() {
    assertThat(Emptiable.isEmpty((Emptiable) () -> false))
        .as("An Emptiable that reports non-empty should not be empty")
        .isFalse();
  }

  @Test
  void emptyCharSequenceIsEmpty() {
    assertThat(Emptiable.isEmpty("")).as("An empty CharSequence should be empty").isTrue();
  }

  @Test
  void nonEmptyCharSequenceIsNotEmpty() {
    assertThat(Emptiable.isEmpty("x")).as("A non-empty CharSequence should not be empty").isFalse();
  }

  @Test
  void emptyCollectionIsEmpty() {
    assertThat(Emptiable.isEmpty(List.of())).as("An empty collection should be empty").isTrue();
  }

  @Test
  void nonEmptyCollectionIsNotEmpty() {
    assertThat(Emptiable.isEmpty(List.of("x")))
        .as("A non-empty collection should not be empty")
        .isFalse();
  }

  @Test
  void emptyMapIsEmpty() {
    assertThat(Emptiable.isEmpty(Map.of())).as("An empty map should be empty").isTrue();
  }

  @Test
  void nonEmptyMapIsNotEmpty() {
    assertThat(Emptiable.isEmpty(Map.of("k", "v")))
        .as("A non-empty map should not be empty")
        .isFalse();
  }

  @Test
  void emptyArrayIsEmpty() {
    assertThat(Emptiable.isEmpty(new String[0])).as("An empty array should be empty").isTrue();
  }

  @Test
  void nonEmptyArrayIsNotEmpty() {
    assertThat(Emptiable.isEmpty(new String[] {"x"}))
        .as("A non-empty array should not be empty")
        .isFalse();
  }

  @Test
  void nonEmptyScalarIsNotEmpty() {
    assertThat(Emptiable.isEmpty(42))
        .as("A non-array, non-empty value should not be empty")
        .isFalse();
  }
}
