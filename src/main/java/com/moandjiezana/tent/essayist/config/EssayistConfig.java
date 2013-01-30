package com.moandjiezana.tent.essayist.config;

import java.util.Properties;

public class EssayistConfig {

    private Properties properties;

    public EssayistConfig(Properties properties) {
        this.properties = properties;
    }

    public String getTitle(){
        return  properties.getProperty("essayist.title");
    }

    public String getLicense(){
        return properties.getProperty("essayist.defaultLicense");
    }
}
