package com.sitepark.ies.aggregator.resolver;

import java.util.List;
import org.jspecify.annotations.Nullable;

public interface EntityResolver extends Resolver {
  int entityId();

  String entityType();

  String entityName();

  @Nullable GroupResolver parentGroup();

  List<GroupResolver> parentGroupPath();
}
