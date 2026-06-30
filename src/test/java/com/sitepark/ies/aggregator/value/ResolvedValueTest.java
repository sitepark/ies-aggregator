package com.sitepark.ies.aggregator.value;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sitepark.ies.aggregator.value.text.PlainText;
import com.sitepark.ies.aggregator.value.text.Text;
import java.util.List;
import java.util.Map;
import java.util.Set;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ResolvedValueTest {

  enum TestEnum {
    ALPHA,
    BETA
  }

  // --- equals/hashCode ----------------------------------------------------

  @Test
  void testEquals() {
    EqualsVerifier.forClass(ResolvedValue.class).verify();
  }

  // --- of() construction --------------------------------------------------

  @Test
  void ofNormalizesEmptyListToEmpty() {
    assertThat(ResolvedValue.of(List.of()).isEmpty())
        .as("of() should treat an empty list as absence of a value")
        .isTrue();
  }

  @Test
  void ofRejectsNonListCollection() {
    assertThatThrownBy(() -> ResolvedValue.of(Set.of("a")))
        .as("of() should only accept List collections, rejecting other Collection types")
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void ofRejectsArrayPayload() {
    assertThatThrownBy(() -> ResolvedValue.of(new Object[] {"a"}))
        .as("of() should reject arrays and require a List instead")
        .isInstanceOf(IllegalArgumentException.class);
  }

  // --- list access --------------------------------------------------------

  @Test
  void isListReportsTrueForListPayload() {
    assertThat(ResolvedValue.of(List.of(1, 2)).isList())
        .as("isList() should be true for a multi-element list payload")
        .isTrue();
  }

  @Test
  void isListReportsFalseForScalarPayload() {
    assertThat(ResolvedValue.of(5).isList())
        .as("isList() should be false for a scalar payload")
        .isFalse();
  }

  @Test
  void asListReturnsTypedElements() {
    assertThat(ResolvedValue.of(List.of("a", "b")).asList(String.class))
        .as("asList() should return the list elements cast to the requested type")
        .containsExactly("a", "b");
  }

  @Test
  void asListOnScalarThrows() {
    assertThatThrownBy(() -> ResolvedValue.of("x").asList(String.class))
        .as("asList() should reject a scalar (non-list) payload")
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void asListWithWrongElementTypeThrows() {
    assertThatThrownBy(() -> ResolvedValue.of(List.of(1, 2)).asList(String.class))
        .as("asList() should reject elements that do not match the requested type")
        .isInstanceOf(ClassCastException.class);
  }

  // --- orElse -------------------------------------------------------------

  @Test
  void orElseReturnsSelfWhenPresent() {
    ResolvedValue present = ResolvedValue.of("x");

    assertThat(present.orElse(() -> ResolvedValue.of("fallback")))
        .as("orElse() should return this value when it is present")
        .isSameAs(present);
  }

  @Test
  void orElseReturnsSupplierResultWhenEmpty() {
    ResolvedValue fallback = ResolvedValue.of("fallback");

    assertThat(ResolvedValue.EMPTY.orElse(() -> fallback))
        .as("orElse() should return the supplier's value when this value is empty")
        .isSameAs(fallback);
  }

  @Test
  void emptyConstantIsEmpty() {
    assertThat(ResolvedValue.EMPTY.isEmpty())
        .as("ResolvedValue.EMPTY should report itself as empty")
        .isTrue();
  }

  @Test
  void valueWithNullPayloadIsEmpty() {
    assertThat(ResolvedValue.of(null).isEmpty())
        .as("ResolvedValue wrapping null should be empty")
        .isTrue();
  }

  @Test
  void valueWithPayloadIsNotEmpty() {
    assertThat(ResolvedValue.of("x").isEmpty())
        .as("ResolvedValue wrapping a non-null payload should not be empty")
        .isFalse();
  }

  @Test
  void valueReturnsWrappedPayload() {
    Object payload = new Object();
    assertThat(ResolvedValue.of(payload).value())
        .as("value() should return the wrapped payload instance")
        .isSameAs(payload);
  }

  @Test
  void valueReturnsNullWhenEmpty() {
    assertThat(ResolvedValue.EMPTY.value()).as("value() should be null when empty").isNull();
  }

  // --- asInt --------------------------------------------------------------

  @Test
  void asIntReturnsIntegerPayload() {
    assertThat(ResolvedValue.of(5).asInt())
        .as("asInt() should return the wrapped Integer value")
        .isEqualTo(5);
  }

  @Test
  void asIntAcceptsIntegralLongPayload() {
    assertThat(ResolvedValue.of(7L).asInt())
        .as("asInt() should accept an integral Long payload")
        .isEqualTo(7);
  }

  @Test
  void asIntOnEmptyThrows() {
    assertThat(ResolvedValue.EMPTY.asInt())
        .as("asInt() without default should return 0")
        .isEqualTo(0);
  }

  @Test
  void asIntWithDefaultReturnsDefaultWhenEmpty() {
    assertThat(ResolvedValue.EMPTY.asInt(99))
        .as("asInt(default) should return the default when empty")
        .isEqualTo(99);
  }

  @Test
  void asIntRejectsNonIntegerPayload() {
    assertThatThrownBy(() -> ResolvedValue.of(1.5).asInt(0))
        .as("asInt() should reject fractional payloads to avoid silent truncation")
        .isInstanceOf(IllegalArgumentException.class);
  }

  // --- asLong -------------------------------------------------------------

  @Test
  void asLongAcceptsIntegerPayload() {
    assertThat(ResolvedValue.of(7).asLong())
        .as("asLong() should widen an Integer payload to long")
        .isEqualTo(7L);
  }

  @Test
  void asLongAcceptsLongPayload() {
    assertThat(ResolvedValue.of(9_999_999_999L).asLong())
        .as("asLong() should return a wrapped Long payload")
        .isEqualTo(9_999_999_999L);
  }

  @Test
  void asLongOnEmptyThrows() {
    assertThat(ResolvedValue.EMPTY.asLong())
        .as("asLong() without default should return 0L")
        .isEqualTo(0L);
  }

  @Test
  void asLongWithDefaultReturnsDefaultWhenEmpty() {
    assertThat(ResolvedValue.EMPTY.asLong(12L))
        .as("asLong(default) should return the default when empty")
        .isEqualTo(12L);
  }

  @Test
  void asLongRejectsNonNumericPayload() {
    assertThatThrownBy(() -> ResolvedValue.of("x").asLong(0L))
        .as("asLong() should reject payloads that are neither Integer nor Long")
        .isInstanceOf(IllegalArgumentException.class);
  }

  // --- asFloat ------------------------------------------------------------

  @Test
  void asFloatAcceptsFloatPayload() {
    assertThat(ResolvedValue.of(1.5f).asFloat())
        .as("asFloat() should return a wrapped Float payload")
        .isEqualTo(1.5f);
  }

  @Test
  void asFloatAcceptsIntegerPayload() {
    assertThat(ResolvedValue.of(3).asFloat())
        .as("asFloat() should widen an Integer payload to float")
        .isEqualTo(3.0f);
  }

  @Test
  void asFloatAcceptsLongPayload() {
    assertThat(ResolvedValue.of(7L).asFloat())
        .as("asFloat() should accept a Long payload")
        .isEqualTo(7.0f);
  }

  @Test
  void asFloatAcceptsDoublePayload() {
    assertThat(ResolvedValue.of(2.5d).asFloat())
        .as("asFloat() should narrow a Double payload to float")
        .isEqualTo(2.5f);
  }

  @Test
  void asFloatOnEmptyThrows() {
    assertThat(ResolvedValue.EMPTY.asFloat())
        .as("asFloat() without default should return 0.0")
        .isEqualTo(0.0f);
  }

  @Test
  void asFloatRejectsNonNumericPayload() {
    assertThatThrownBy(() -> ResolvedValue.of("x").asFloat(0f))
        .as("asFloat() should reject non-Number payloads")
        .isInstanceOf(IllegalArgumentException.class);
  }

  // --- asDouble -----------------------------------------------------------

  @Test
  void asDoubleAcceptsDoublePayload() {
    assertThat(ResolvedValue.of(2.5d).asDouble())
        .as("asDouble() should return the wrapped Double value")
        .isEqualTo(2.5d);
  }

  @Test
  void asDoubleWidensInteger() {
    assertThat(ResolvedValue.of(4).asDouble())
        .as("asDouble() should widen any Number subtype to double")
        .isEqualTo(4.0d);
  }

  @Test
  void asDoubleOnEmptyThrows() {
    assertThat(ResolvedValue.EMPTY.asDouble())
        .as("asDouble() without default should return 0.0")
        .isEqualTo(0.0d);
  }

  @Test
  void asDoubleRejectsNonNumericPayload() {
    assertThatThrownBy(() -> ResolvedValue.of("x").asDouble(0d))
        .as("asDouble() should reject non-Number payloads")
        .isInstanceOf(IllegalArgumentException.class);
  }

  // --- asString -----------------------------------------------------------

  @Test
  void asStringReturnsToStringOfPayload() {
    assertThat(ResolvedValue.of(42).asString())
        .as("asString() should use payload's toString()")
        .isEqualTo("42");
  }

  @Test
  void asStringOnEmptyThrows() {
    assertThat(ResolvedValue.EMPTY.asString())
        .as("asString() without default should return empty string")
        .isEqualTo("");
  }

  @Test
  void asStringWithDefaultReturnsDefaultWhenEmpty() {
    assertThat(ResolvedValue.EMPTY.asString("fallback"))
        .as("asString(default) should return the default when empty")
        .isEqualTo("fallback");
  }

  // --- asText -------------------------------------------------------------

  @Test
  void asTextReturnsTextInstanceUnchanged() {
    Text text = Text.of("hello");
    assertThat(ResolvedValue.of(text).asText())
        .as("asText() should return a wrapped Text as the same instance")
        .isSameAs(text);
  }

  @Test
  void asTextWrapsStringPayload() {
    assertThat(ResolvedValue.of("hi").asText().toString())
        .as("asText() should wrap a String payload into a new Text")
        .isEqualTo("hi");
  }

  @Test
  void asTextOnEmptyThrows() {
    assertThat(ResolvedValue.EMPTY.asText().toString())
        .as("asText() without default should return empty string")
        .isEqualTo("");
  }

  @Test
  void asTextWithDefaultReturnsDefaultWhenEmpty() {
    PlainText def = PlainText.of("def");
    assertThat(ResolvedValue.EMPTY.asText(def))
        .as("asText(default) should return the default when empty")
        .isSameAs(def);
  }

  @Test
  void asTextRejectsUnsupportedPayload() {
    assertThatThrownBy(() -> ResolvedValue.of(123).asText(PlainText.EMPTY))
        .as("asText() should reject payloads that are neither Text nor String")
        .isInstanceOf(IllegalArgumentException.class);
  }

  // --- asEnum -------------------------------------------------------------

  @Test
  void asEnumReturnsEnumInstanceUnchanged() {
    assertThat(ResolvedValue.of(TestEnum.ALPHA).asEnum(TestEnum.class))
        .as("asEnum() should return a wrapped enum constant as the same instance")
        .isSameAs(TestEnum.ALPHA);
  }

  @Test
  void asEnumParsesStringPayload() {
    assertThat(ResolvedValue.of("BETA").asEnum(TestEnum.class))
        .as("asEnum() should parse a String payload to the matching enum constant")
        .isEqualTo(TestEnum.BETA);
  }

  @Test
  void asEnumOnEmptyThrows() {
    assertThatThrownBy(() -> ResolvedValue.EMPTY.asEnum(TestEnum.class))
        .as("asEnum() without default should reject empty value")
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void asEnumWithDefaultReturnsDefaultWhenEmpty() {
    assertThat(ResolvedValue.EMPTY.asEnum(TestEnum.class, TestEnum.ALPHA))
        .as("asEnum(default) should return the default when empty")
        .isSameAs(TestEnum.ALPHA);
  }

  @Test
  void asEnumRejectsUnsupportedPayload() {
    assertThatThrownBy(() -> ResolvedValue.of(7).asEnum(TestEnum.class, null))
        .as("asEnum() should reject payloads that are neither the enum type nor a String")
        .isInstanceOf(IllegalArgumentException.class);
  }

  // --- asBoolean ----------------------------------------------------------

  @Test
  void asBooleanReturnsBooleanPayload() {
    assertThat(ResolvedValue.of(true).asBoolean())
        .as("asBoolean() should return a wrapped Boolean payload unchanged")
        .isTrue();
  }

  @Test
  void asBooleanParsesStringPayload() {
    assertThat(ResolvedValue.of("true").asBoolean())
        .as("asBoolean() should parse a String payload via Boolean.parseBoolean")
        .isTrue();
  }

  @Test
  void asBooleanOnEmptyThrows() {
    assertThat(ResolvedValue.EMPTY.asBoolean())
        .as("asBoolean() without default should return false")
        .isFalse();
  }

  @Test
  void asBooleanWithDefaultReturnsDefaultWhenEmpty() {
    assertThat(ResolvedValue.EMPTY.asBoolean(true))
        .as("asBoolean(default) should return the default when empty")
        .isTrue();
  }

  @Test
  void asBooleanRejectsUnsupportedPayload() {
    assertThatThrownBy(() -> ResolvedValue.of(1).asBoolean(false))
        .as("asBoolean() should reject payloads that are neither Boolean nor String")
        .isInstanceOf(IllegalArgumentException.class);
  }

  // --- scalar accessors on list payloads ----------------------------------

  @Test
  void scalarAccessorUnwrapsSingleElementList() {
    assertThat(ResolvedValue.of(List.of(5)).asInt())
        .as("A scalar accessor should unwrap a single-element list to that element")
        .isEqualTo(5);
  }

  @Test
  void scalarAccessorRejectsMultiElementList() {
    assertThatThrownBy(() -> ResolvedValue.of(List.of(1, 2, 3)).asInt())
        .as(
            "A scalar accessor should reject a multi-element list rather than silently "
                + "using the first element")
        .isInstanceOf(IllegalArgumentException.class);
  }

  // --- as / asMap (structured) --------------------------------------------

  /** Parser that fails the test if invoked — used to prove no parsing happens on a cast path. */
  private static final StructuredValueParser FAILING_PARSER =
      new StructuredValueParser() {
        @Override
        public <T> T parse(String raw, Class<T> type) {
          throw new AssertionError("parser should not be invoked");
        }
      };

  /** Parser that ignores its input and returns a fixed map, standing in for a real JSON parser. */
  private static final StructuredValueParser FIXED_MAP_PARSER =
      new StructuredValueParser() {
        @Override
        public <T> T parse(String raw, Class<T> type) {
          return type.cast(Map.of("foo", "bar"));
        }
      };

  @Test
  void asCastsAlreadyAssignablePayloadWithoutParsing() {
    Map<String, Object> payload = Map.of("a", 1);
    assertThat(ResolvedValue.of(payload).as(Map.class, FAILING_PARSER))
        .as("as() should return an already-assignable payload as-is, without invoking the parser")
        .isSameAs(payload);
  }

  @Test
  void asParsesStringPayloadViaParser() {
    assertThat(ResolvedValue.of("{\"foo\":\"bar\"}").as(Map.class, FIXED_MAP_PARSER))
        .as("as() should deserialize a String payload through the parser")
        .isEqualTo(Map.of("foo", "bar"));
  }

  @Test
  void asOnEmptyThrows() {
    assertThatThrownBy(() -> ResolvedValue.EMPTY.as(Map.class, FIXED_MAP_PARSER))
        .as("as() should reject an empty value")
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void asRejectsPayloadThatIsNeitherAssignableNorString() {
    assertThatThrownBy(() -> ResolvedValue.of(123).as(Map.class, FIXED_MAP_PARSER))
        .as("as() should reject a payload that is neither assignable to the type nor a String")
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void asMapReturnsExistingMapUnchanged() {
    Map<String, Object> payload = Map.of("a", 1);
    assertThat(ResolvedValue.of(payload).asMap(FAILING_PARSER))
        .as("asMap() should return an existing Map payload unchanged, without invoking the parser")
        .isSameAs(payload);
  }

  @Test
  void asMapParsesStringPayloadViaParser() {
    assertThat(ResolvedValue.of("{\"foo\":\"bar\"}").asMap(FIXED_MAP_PARSER))
        .as("asMap() should deserialize a JSON String payload to a Map through the parser")
        .isEqualTo(Map.of("foo", "bar"));
  }

  @Test
  void asUnwrapsSingleElementListBeforeConverting() {
    assertThat(ResolvedValue.of(List.of("{\"foo\":\"bar\"}")).asMap(FIXED_MAP_PARSER))
        .as("as()/asMap() should unwrap a single-element list before converting")
        .isEqualTo(Map.of("foo", "bar"));
  }
}
