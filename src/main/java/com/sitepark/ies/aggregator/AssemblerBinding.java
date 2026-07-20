package com.sitepark.ies.aggregator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AssemblerBinding {
  String value();

  /**
   * Priority for overriding. Higher values override lower ones. Defaults allow custom assemblers
   * (e.g. priority 100) to override built-in ones (priority 0).
   *
   * <p>Within a chain (see {@link com.sitepark.ies.aggregator.port.AssemblerFactory#createChain})
   * the priority also defines the execution order: assemblers run in ascending priority order, so a
   * higher-priority assembler runs later and gets the last word.
   */
  int priority() default 0;

  /**
   * Marks this assembler as a chain root within {@link
   * com.sitepark.ies.aggregator.port.AssemblerFactory#createChain}: all assemblers with a lower
   * {@link #priority()} are skipped (neither executed nor instantiated), because this assembler
   * produces a fresh value from scratch and would discard their results. If several roots exist, the
   * one with the highest priority wins.
   */
  boolean chainRoot() default false;

  /**
   * Marks this assembler as the last one in a chain within {@link
   * com.sitepark.ies.aggregator.port.AssemblerFactory#createChain}: all assemblers with a higher
   * {@link #priority()} are skipped (neither executed nor instantiated), so this assembler is
   * guaranteed to run last. If several are marked, the one with the lowest priority wins.
   */
  boolean chainBreak() default false;

  /**
   * Restricts this assembler to the given CMS object types. An empty array (the default) means the
   * assembler applies to every object type. The object type is derived from the current resolver
   * scope at selection time (see {@link
   * com.sitepark.ies.aggregator.port.AssemblerFactory#createChain(String, Class,
   * com.sitepark.ies.aggregator.resolver.Resolver)}).
   */
  String[] objectTypes() default {};

  /**
   * A custom applicability rule for rare cases that a simple {@link #objectTypes()} match cannot
   * express. The factory instantiates the class via dependency injection and calls {@link
   * AssemblerCondition#appliesTo}; the condition's own dependencies are supplied through its
   * constructor. The default {@link AssemblerCondition.Always} always applies and is never
   * instantiated.
   */
  Class<? extends AssemblerCondition> condition() default AssemblerCondition.Always.class;
}
