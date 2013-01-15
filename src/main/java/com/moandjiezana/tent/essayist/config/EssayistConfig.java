package com.moandjiezana.tent.essayist.config;

import java.util.Properties;

/**
 * User: pjesi
 * Date: 1/14/13
 * Time: 11:11 PM
 */
public class EssayistConfig {

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

}
