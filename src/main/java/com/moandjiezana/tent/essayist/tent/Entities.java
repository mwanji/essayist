package com.moandjiezana.tent.essayist.tent;

import com.google.common.base.Strings;
import com.moandjiezana.tent.client.users.Profile;

public class Entities {
  
  public static String stripScheme(String entity) {
    return entity.replace("http://", "").replace("https://", "");
  }

  public static String getForUrl(String entity) {
    String prefix = entity.startsWith("http://") ? "h:" : "";
    String urlEntity = entity.replace("http://", "").replace("https://", "");
    if (urlEntity.endsWith("/")) {
      urlEntity = urlEntity.substring(0, urlEntity.length() - 1);
    }

    return prefix + urlEntity;
  }
  
  public static String expandFromUrl(String entity) {
    String expandedEntity;
    if (entity.startsWith("h:")) {
      expandedEntity = "http://" + entity.substring(2);
    } else {
      expandedEntity = "https://" + entity;
    }
    
    if (expandedEntity.endsWith("/")) {
      expandedEntity = expandedEntity.substring(0, expandedEntity.length() - 1);
    }
    
    return expandedEntity;
  }
  
  public static String getName(Profile profile) {
    return getName(profile, profile.getCore().getEntity());
  }
  
  public static String getName(Profile profile, String fallback) {
    return profile != null && profile.getBasic() != null && !Strings.isNullOrEmpty(profile.getBasic().getName()) ? profile.getBasic().getName() : fallback;
  }
}
