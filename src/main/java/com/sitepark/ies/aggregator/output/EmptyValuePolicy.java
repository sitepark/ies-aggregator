package com.sitepark.ies.aggregator.output;

import java.util.List;
import java.util.Objects;

/**
 * Strategy that decides — by the value's runtime class — which empty values are rendered anyway.
 *
 * <p>Empty values are dropped from the output by default (see {@link
 * OutputVisitor#rendersEmpty(Object)}). Consumers that rely on a key being present even when its
 * value is empty (backwards compatibility) need a way to say <b>which</b> empty values survive,
 * without touching the value classes themselves. This policy is that seam: the {@link OutputVisitor}
 * consults it for every value before applying the recursive emptiness rule, and a value the policy
 * keeps is never treated as empty.
 *
 * <p>{@link #ANNOTATED} is the default and reproduces the behavior that existed before this policy:
 * a type carrying {@link OutputKeepIfEmpty} is kept, everything else is dropped when empty. Custom
 * rules are usually combined with it:
 *
 * <pre>{@code
 * EmptyValuePolicy policy =
 *     EmptyValuePolicy.ANNOTATED.or(EmptyValuePolicy.keepTypes(Text.class, Uri.class));
 * new JsonWriter(writer, mapper, translations, policy);
 * }</pre>
 *
 * <p>The decision is made on the class only, so it cannot depend on the concrete value. Keeping a
 * single occurrence empty — the same type dropped elsewhere — is a different concern, expressed with
 * the property-level {@link OutputKeepIfEmpty} and its {@link KeepEmpty} wrapper.
 *
 * <p>Because emptiness is evaluated recursively, a kept empty value makes its surrounding container
 * non-empty: the ancestors of a kept value survive the pruning as well.
 */
@FunctionalInterface
public interface EmptyValuePolicy {

  /**
   * Default policy: keeps an empty value whose runtime class carries the type-level {@link
   * OutputKeepIfEmpty}, drops every other empty value.
   */
  EmptyValuePolicy ANNOTATED = type -> type.isAnnotationPresent(OutputKeepIfEmpty.class);

  /**
   * Policy that keeps nothing: every empty value is dropped, even one whose type carries {@link
   * OutputKeepIfEmpty}.
   */
  EmptyValuePolicy DROP_ALL = type -> false;

  /**
   * Policy that keeps everything — including {@code null} — so the output mirrors the tree as built.
   * Useful for consumers that expect a fixed set of keys regardless of their content.
   */
  EmptyValuePolicy KEEP_ALL =
      new EmptyValuePolicy() {

        @Override
        public boolean keepIfEmpty(Class<?> type) {
          return true;
        }

        @Override
        public boolean keepNull() {
          return true;
        }
      };

  /**
   * The policy that applies once the visitor descends into a domain object — the properties of a
   * model the aggregator assembled, rather than values a caller handed it.
   *
   * <p>Answers {@code this} by default: a rule about which empty values survive normally holds one
   * level down as well. Override it where a policy is a statement about <i>whose</i> data it
   * governs rather than about the values themselves — a caller that needs its own empty keys to
   * stay put has said nothing about the models it did not write.
   *
   * <p>The switch applies from the moment a domain object — or a {@link
   * com.sitepark.ies.aggregator.value.text.TranslatableContainer} rendering one — is entered, and it
   * nests: the returned policy governs everything below, including further domain objects, and the
   * previous one is restored on the way out.
   *
   * @return the policy governing the properties of a domain object; {@code this} unless a policy
   *     says otherwise
   */
  default EmptyValuePolicy insideDomainObject() {
    return this;
  }

  /**
   * Returns a policy that keeps every empty value assignable to one of {@code types} — so naming an
   * interface of a sealed hierarchy (e.g. {@code Text}) covers all of its variants.
   *
   * <p>Called without arguments the returned policy keeps nothing, like {@link #DROP_ALL}.
   *
   * @param types the types to keep when empty; must not contain {@code null}
   * @return a policy keeping empty values of the given types
   * @throws NullPointerException if {@code types} or one of its elements is {@code null}
   */
  static EmptyValuePolicy keepTypes(Class<?>... types) {
    List<Class<?>> kept = List.of(types);
    return type -> {
      for (Class<?> keptType : kept) {
        if (keptType.isAssignableFrom(type)) {
          return true;
        }
      }
      return false;
    };
  }

  /**
   * Returns a policy that keeps an empty value when this policy or {@code other} keeps it.
   *
   * @param other the policy to combine with; must not be {@code null}
   * @return the combined policy
   */
  default EmptyValuePolicy or(EmptyValuePolicy other) {
    Objects.requireNonNull(other);
    return new EmptyValuePolicy() {

      @Override
      public boolean keepIfEmpty(Class<?> type) {
        return EmptyValuePolicy.this.keepIfEmpty(type) || other.keepIfEmpty(type);
      }

      @Override
      public boolean keepNull() {
        return EmptyValuePolicy.this.keepNull() || other.keepNull();
      }

      // Combines what both sides apply below a domain object, so neither side loses its rule.
      @Override
      public EmptyValuePolicy insideDomainObject() {
        return EmptyValuePolicy.this.insideDomainObject().or(other.insideDomainObject());
      }
    };
  }

  /**
   * Returns {@code true} if a {@code null} value must be rendered even though it is empty.
   *
   * <p>A {@code null} carries no class, so it cannot be decided by {@link #keepIfEmpty(Class)}. The
   * default drops it — the behavior of every policy but {@link #KEEP_ALL}. A single {@code null}
   * that must be rendered is better expressed per occurrence with {@link KeepEmpty}.
   *
   * @return {@code true} to render {@code null} values, {@code false} to drop them
   */
  default boolean keepNull() {
    return false;
  }

  /**
   * Returns {@code true} if a value of {@code type} must be rendered even when it is empty.
   *
   * @param type the runtime class of the empty value; never {@code null}
   * @return {@code true} to keep the value, {@code false} to drop it
   */
  boolean keepIfEmpty(Class<?> type);
}
