package com.sitepark.ies.aggregator.output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sitepark.ies.aggregator.value.Emptiable;
import com.sitepark.ies.aggregator.value.text.PlainText;
import com.sitepark.ies.aggregator.value.text.Text;
import com.sitepark.ies.aggregator.value.uri.PlainUri;
import org.junit.jupiter.api.Test;

class EmptyValuePolicyTest {

  @OutputKeepIfEmpty
  record KeptFlag() implements Emptiable {
    @Override
    public boolean isEmpty() {
      return true;
    }
  }

  record PlainFlag() {}

  @Test
  void annotatedPolicyKeepsAnnotatedType() {
    assertThat(EmptyValuePolicy.ANNOTATED.keepIfEmpty(KeptFlag.class))
        .as("A type carrying @OutputKeepIfEmpty should be kept by the default policy")
        .isTrue();
  }

  @Test
  void annotatedPolicyDropsUnannotatedType() {
    assertThat(EmptyValuePolicy.ANNOTATED.keepIfEmpty(PlainFlag.class))
        .as("A type without @OutputKeepIfEmpty should be dropped by the default policy")
        .isFalse();
  }

  @Test
  void dropAllKeepsNothing() {
    assertThat(EmptyValuePolicy.DROP_ALL.keepIfEmpty(KeptFlag.class))
        .as("DROP_ALL should drop even an @OutputKeepIfEmpty type")
        .isFalse();
  }

  @Test
  void keepAllKeepsEverything() {
    assertThat(EmptyValuePolicy.KEEP_ALL.keepIfEmpty(PlainFlag.class))
        .as("KEEP_ALL should keep an empty value of any type")
        .isTrue();
  }

  @Test
  void keepAllKeepsNull() {
    assertThat(EmptyValuePolicy.KEEP_ALL.keepNull())
        .as("KEEP_ALL should also keep null values")
        .isTrue();
  }

  @Test
  void otherPoliciesDropNull() {
    assertThat(EmptyValuePolicy.ANNOTATED.keepNull())
        .as("The default policy should drop null values")
        .isFalse();
    assertThat(EmptyValuePolicy.DROP_ALL.keepNull())
        .as("DROP_ALL should drop null values")
        .isFalse();
    assertThat(EmptyValuePolicy.keepTypes(Text.class).keepNull())
        .as("A type-based policy should drop null values")
        .isFalse();
  }

  @Test
  void orKeepsNullWhenEitherPolicyKeepsIt() {
    assertThat(EmptyValuePolicy.ANNOTATED.or(EmptyValuePolicy.KEEP_ALL).keepNull())
        .as("A combined policy should keep null when either policy keeps it")
        .isTrue();
    assertThat(EmptyValuePolicy.ANNOTATED.or(EmptyValuePolicy.DROP_ALL).keepNull())
        .as("A combined policy should drop null when neither policy keeps it")
        .isFalse();
  }

  @Test
  void keepTypesMatchesSubtypes() {
    EmptyValuePolicy policy = EmptyValuePolicy.keepTypes(Text.class);

    assertThat(policy.keepIfEmpty(PlainText.class))
        .as("Naming an interface should keep every type assignable to it")
        .isTrue();
  }

  @Test
  void keepTypesMatchesTheNamedTypeItself() {
    EmptyValuePolicy policy = EmptyValuePolicy.keepTypes(PlainText.class);

    assertThat(policy.keepIfEmpty(PlainText.class))
        .as("The named type itself should be kept")
        .isTrue();
  }

  @Test
  void keepTypesDoesNotMatchUnrelatedTypes() {
    EmptyValuePolicy policy = EmptyValuePolicy.keepTypes(Text.class);

    assertThat(policy.keepIfEmpty(PlainUri.class))
        .as("A type not assignable to any named type should be dropped")
        .isFalse();
  }

  @Test
  void keepTypesWithoutArgumentsKeepsNothing() {
    assertThat(EmptyValuePolicy.keepTypes().keepIfEmpty(PlainText.class))
        .as("keepTypes() without arguments should keep nothing")
        .isFalse();
  }

  @Test
  void keepTypesRejectsNullType() {
    assertThatThrownBy(() -> EmptyValuePolicy.keepTypes((Class<?>) null))
        .as("keepTypes should fail fast on a null type")
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void orKeepsWhenTheFirstPolicyKeeps() {
    EmptyValuePolicy policy = EmptyValuePolicy.ANNOTATED.or(EmptyValuePolicy.keepTypes(Text.class));

    assertThat(policy.keepIfEmpty(KeptFlag.class))
        .as("A combined policy should keep what its first policy keeps")
        .isTrue();
  }

  @Test
  void orKeepsWhenTheSecondPolicyKeeps() {
    EmptyValuePolicy policy = EmptyValuePolicy.ANNOTATED.or(EmptyValuePolicy.keepTypes(Text.class));

    assertThat(policy.keepIfEmpty(PlainText.class))
        .as("A combined policy should keep what its second policy keeps")
        .isTrue();
  }

  @Test
  void orDropsWhenNeitherPolicyKeeps() {
    EmptyValuePolicy policy = EmptyValuePolicy.ANNOTATED.or(EmptyValuePolicy.keepTypes(Text.class));

    assertThat(policy.keepIfEmpty(PlainUri.class))
        .as("A combined policy should drop what neither policy keeps")
        .isFalse();
  }

  @Test
  void orRejectsNullPolicy() {
    assertThatThrownBy(() -> EmptyValuePolicy.ANNOTATED.or(null))
        .as("or should fail fast on a null policy")
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void mostPoliciesApplyUnchangedInsideADomainObject() {
    assertThat(EmptyValuePolicy.ANNOTATED.insideDomainObject())
        .as("a rule about which empties survive holds one level down as well")
        .isSameAs(EmptyValuePolicy.ANNOTATED);
    assertThat(EmptyValuePolicy.DROP_ALL.insideDomainObject())
        .as("dropping everything means dropping everything, at any depth")
        .isSameAs(EmptyValuePolicy.DROP_ALL);

    EmptyValuePolicy keepText = EmptyValuePolicy.keepTypes(CharSequence.class);
    assertThat(keepText.insideDomainObject())
        .as("a type rule is not a statement about who produced the value")
        .isSameAs(keepText);
  }

  @Test
  void keepAllStepsBackToTheDefaultInsideADomainObject() {
    assertThat(EmptyValuePolicy.KEEP_ALL.insideDomainObject())
        .as(
            "keeping every empty value is a compatibility setting for the caller's data, not for"
                + " the models the aggregator assembled")
        .isSameAs(EmptyValuePolicy.ANNOTATED);
  }

  @Test
  void combiningKeepsBothRulesInsideADomainObject() {
    EmptyValuePolicy combined =
        EmptyValuePolicy.KEEP_ALL.or(EmptyValuePolicy.keepTypes(CharSequence.class));

    EmptyValuePolicy inside = combined.insideDomainObject();

    assertThat(inside.keepIfEmpty(String.class))
        .as("the combined type rule survives below a domain object")
        .isTrue();
    assertThat(inside.keepIfEmpty(Integer.class))
        .as("what only KEEP_ALL kept does not survive below a domain object")
        .isFalse();
  }
}
