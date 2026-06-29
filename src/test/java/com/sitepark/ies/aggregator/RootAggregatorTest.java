package com.sitepark.ies.aggregator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sitepark.ies.aggregator.output.OutputNode;
import com.sitepark.ies.aggregator.output.OutputObject;
import com.sitepark.ies.aggregator.resolver.Resolver;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class RootAggregatorTest {

  /**
   * Test double that records the OutputObject it was invoked with — used to verify that all
   * aggregators share the same target instance.
   */
  private static final class RecordingAggregator implements Aggregator {
    OutputNode received;

    @Override
    public void aggregate(Resolver source, OutputNode aggregation) {
      this.received = aggregation;
    }
  }

  @Test
  void aggregateReturnsFreshRootOutputObject() {
    RootAggregator rootAggregator = new RootAggregator(List.of());

    OutputObject result = rootAggregator.aggregate(Resolver.empty());

    assertThat(result).as("aggregate() should always return a non-null OutputObject").isNotNull();
    assertThat(result.path()).as("Returned OutputObject should be the root (empty path)").isEmpty();
  }

  @Test
  void aggregatorsRunInRegisteredOrder() {
    Aggregator first = mock(Aggregator.class);
    Aggregator second = mock(Aggregator.class);

    new RootAggregator(List.of(first, second)).aggregate(Resolver.empty());

    InOrder order = inOrder(first, second);
    order.verify(first).aggregate(eq(Resolver.empty()), any(OutputNode.class));
    order.verify(second).aggregate(eq(Resolver.empty()), any(OutputNode.class));
  }

  @Test
  void allAggregatorsReceiveTheSameSharedOutputInstance() {
    RecordingAggregator first = new RecordingAggregator();
    RecordingAggregator second = new RecordingAggregator();

    OutputObject result = new RootAggregator(List.of(first, second)).aggregate(Resolver.empty());

    assertThat(first.received)
        .as("First aggregator should receive the same OutputObject that is returned")
        .isSameAs(result);
    assertThat(second.received)
        .as("All aggregators should receive the same shared OutputObject instance")
        .isSameAs(result);
  }

  @Test
  void writesByEarlierAggregatorsAreVisibleToLaterAggregators() {
    List<String> seenByLater = new ArrayList<>();
    Aggregator writer = (source, out) -> out.put("greeting", "hello");
    Aggregator reader = (source, out) -> seenByLater.add(out.getString("greeting"));

    new RootAggregator(List.of(writer, reader)).aggregate(Resolver.empty());

    assertThat(seenByLater)
        .as("Later aggregator should see writes made by earlier aggregators on the shared output")
        .containsExactly("hello");
  }

  @Test
  void failingAggregatorPropagatesException() {
    Aggregator failing = mock(Aggregator.class);
    AggregatorException boom = new AggregatorException("boom");
    doThrow(boom).when(failing).aggregate(any(), any());

    assertThatThrownBy(() -> new RootAggregator(List.of(failing)).aggregate(Resolver.empty()))
        .as("Exception from an aggregator should propagate unchanged out of aggregate()")
        .isSameAs(boom);
  }

  @Test
  void laterAggregatorsAreSkippedAfterFailure() {
    Aggregator failing = mock(Aggregator.class);
    Aggregator later = mock(Aggregator.class);
    doThrow(new AggregatorException("boom")).when(failing).aggregate(any(), any());

    assertThatThrownBy(
            () -> new RootAggregator(List.of(failing, later)).aggregate(Resolver.empty()))
        .isInstanceOf(AggregatorException.class);

    verify(later, never()).aggregate(any(), any());
  }
}
