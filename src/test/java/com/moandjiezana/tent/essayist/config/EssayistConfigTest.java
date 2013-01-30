package com.moandjiezana.tent.essayist.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

/**
 * User: pjesi
 * Date: 1/17/13
 * Time: 9:05 PM
 */
public class EssayistConfigTest {

    private Properties properties;
    private EssayistConfig config;

    @Before
    public void setup(){
        properties = new Properties();
        config = new EssayistConfig(properties);
    }

    @Test
    public void should_return_no_default_entity(){
        assertFalse(config.getDefaultEntity().isPresent());

    }

    @Test
    public void should_return_default_entity(){
        properties.setProperty(EssayistConfig.DEFAULT_ENTITY, "http://pjesi.com");
        assertEquals("http://pjesi.com",config.getDefaultEntity().get());
    }

}
