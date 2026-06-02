package com.sitepark.ies.aggregator;

public interface OptionsAware<O extends Options> {
  void setOptions(O options);
}
