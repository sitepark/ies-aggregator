package com.sitepark.ies.aggregator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Assembler {
  String value();

  /**
   * Priority for overriding. Higher values override lower ones. Defaults allow custom assemblers
   * (e.g. priority 100) to override built-in ones (priority 0).
   */
  int priority() default 0;
}
