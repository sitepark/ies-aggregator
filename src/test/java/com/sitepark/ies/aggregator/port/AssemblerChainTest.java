package com.sitepark.ies.aggregator.port;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class AssemblerChainTest {

  @Test
  void foldThreadsPreviousThroughEveryAssembler() {
    AssemblerChain<Function<String, Optional<String>>> chain =
        new AssemblerChain<>(
            List.of(
                previous -> Optional.of("base"),
                previous -> Optional.ofNullable(previous).map(value -> value + "+a"),
                previous -> Optional.ofNullable(previous).map(value -> value + "+b")));

    Optional<String> result = chain.fold((assembler, previous) -> assembler.apply(previous));

    assertThat(result)
        .as("each assembler should receive and extend the previous value in order")
        .contains("base+a+b");
  }

  @Test
  void foldOnEmptyChainYieldsEmptyResult() {
    AssemblerChain<Function<String, Optional<String>>> chain = new AssemblerChain<>(List.of());

    Optional<String> result = chain.fold((assembler, previous) -> assembler.apply(previous));

    assertThat(result).as("an empty chain should produce no value").isEmpty();
  }

  @Test
  void laterAssemblerCanReplacePreviousValue() {
    AssemblerChain<Function<String, Optional<String>>> chain =
        new AssemblerChain<>(
            List.of(previous -> Optional.of("base"), previous -> Optional.of("replaced")));

    Optional<String> result = chain.fold((assembler, previous) -> assembler.apply(previous));

    assertThat(result)
        .as("an assembler that ignores previous replaces the accumulated value")
        .contains("replaced");
  }

  @Test
  void isEmptyReflectsTheAssemblerList() {
    assertThat(new AssemblerChain<>(List.of()).isEmpty())
        .as("a chain without assemblers is empty")
        .isTrue();
    assertThat(new AssemblerChain<>(List.of("a")).isEmpty())
        .as("a chain with an assembler is not empty")
        .isFalse();
  }
}
