package com.moandjiezana.tent.essayist.tent;

public class Entities {

  public static String getEntityForUrl(String entity) {
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
}
