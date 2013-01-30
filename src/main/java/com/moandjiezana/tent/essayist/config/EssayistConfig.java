package com.moandjiezana.tent.essayist.config;

import com.google.common.base.Optional;

import java.util.Properties;

/**
 * User: pjesi
 * Date: 1/14/13
 * Time: 11:11 PM
 */
public class EssayistConfig {

    public static final String BASE_DOMAIN = "essayist.domain.base";
    public static final String DEFAULT_ENTITY = "essayist.defaultEntity";


    private Properties properties;

    public EssayistConfig(Properties properties) {
        this.properties = properties;
    }

    public String getTitle(){
        return  properties.getProperty("essayist.title", "Essayist");
    }

    public String getLicense(){
        return properties.getProperty("essayist.defaultLicense", "http://creativecommons.org/licenses/by/3.0/");
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
        return defaultEntity.or("").equals(entity);
    }
}
