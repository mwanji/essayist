package com.moandjiezana.tent.essayist.tent;

public class Entities {

  public static String getEntityForUrl(String entity) {
    String prefix = entity.startsWith("https://") ? "s:" : "";
    String urlEntity = entity.replace("http://", "").replace("https://", "");
    if (urlEntity.endsWith("/")) {
      urlEntity.substring(0, urlEntity.length() - 1);
    }

    return prefix + urlEntity;
  }
  
  public static String expandFromUrl(String entity) {
    if (entity.startsWith("s:")) {
      return "https://" + entity.substring(2);
    } else {
      return "http://" + entity;
    }
  }
}
