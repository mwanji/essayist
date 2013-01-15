package com.moandjiezana.tent.essayist.config;

import com.moandjiezana.tent.essayist.User;
import com.moandjiezana.tent.essayist.user.UserService;
import fj.data.Option;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.Properties;

import static fj.data.Option.none;
import static fj.data.Option.some;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: pjesi
 * Date: 1/15/13
 * Time: 8:29 PM
 */
public class EntityLookupTest {


    EntityLookup lookup;
    Properties properties;
    EssayistConfig config;
    UserService userService;

    @Before
    public void setup(){
        properties = new Properties();
        config = new EssayistConfig(properties);
        userService = mock(UserService.class);
        lookup = new EntityLookup(config, userService);

    }

    @Test
    public void should_return_none_if_nothing_is_configured(){
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn("localhost");
        Option<String> profile = lookup.getEntity(request);
        assertTrue(profile.isNone());
    }

    @Test
    public void should_return_none_if_base_domain_is_requested(){
        properties.setProperty(EssayistConfig.BASE_DOMAIN, "localhost");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn("localhost");
        Option<String> profile = lookup.getEntity(request);
        assertTrue(profile.isNone());
    }

    @Test
    public void should_return_profile_if_sub_domain_is_requested(){

        User user = new User("http://pjesi.com");
        when(userService.getUserByDomain("subdomain.localhost"))
                .thenReturn(some(user));

        properties.setProperty(EssayistConfig.BASE_DOMAIN, "localhost");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn("subdomain.localhost");
        Option<String> entity = lookup.getEntity(request);
        assertTrue(entity.isSome());
    }

    @Test
    public void should_not_return_profile_if_sub_domain_is_unknown(){


        when(userService.getUserByDomain("subdomain.localhost"))
                .thenReturn(Option.<User>none());

        properties.setProperty(EssayistConfig.BASE_DOMAIN, "localhost");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn("subdomain.localhost");
        Option<String> entity = lookup.getEntity(request);
        assertTrue(entity.isNone());

    }

}