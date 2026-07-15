package com.sitepark.ies.aggregator.resolver;

import java.util.List;

public interface GroupResolver extends EntityResolver {
  List<GroupResolver> groupSubGroups();

  List<EntityResolver> groupEntities();

  List<EntityResolver> groupChildren();

  boolean isRootSiteGroup();

  boolean isMicrositeRootSiteGroup();

  String lang();

  default boolean isGroupPathRoot() {
    return parentGroup() == null;
  }
}
