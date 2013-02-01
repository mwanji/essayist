package com.moandjiezana.essayist.config;

import com.google.common.base.Optional;

import java.util.Properties;

public class EssayistConfig {

  private static final String APP_NAME = "essayist.title";
  private static final String DEFAULT_LICENSE = "essayist.defaultLicense";
  private static final String BASE_DOMAIN = "essayist.domain.base";
  private static final String DEFAULT_ENTITY = "essayist.defaultEntity";

  private Properties properties;

  public EssayistConfig(Properties properties) {
      this.properties = properties;
  }

  public String getTitle(){
      return  properties.getProperty(APP_NAME);
  }

  public String getLicense(){
      return properties.getProperty(DEFAULT_LICENSE);
  }

  public String getBaseDomain(String defaultDomain) {
      return properties.getProperty(BASE_DOMAIN, defaultDomain);
  }

  public Optional<String> getDefaultEntity(){
      String defaultEntity = properties.getProperty(DEFAULT_ENTITY);
      return Optional.of(defaultEntity);
  }

  public boolean isDefaultEntity(String entity){
    Optional<String> defaultEntity = getDefaultEntity();
      return defaultEntity.isPresent() && defaultEntity.get().equals(entity);
  }
}
