package com.moandjiezana.tent.essayist.config;

import static com.google.common.base.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.moandjiezana.tent.essayist.User;
import com.moandjiezana.tent.essayist.user.UserService;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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

    private void setPath(HttpServletRequest request, String path){
        when(request.getPathInfo()).thenReturn(path);
        when(request.getRequestURI()).thenReturn(path);
        when(request.getContextPath()).thenReturn("");
    }

    @Test
    public void should_return_none_if_nothing_is_configured(){
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn("localhost");
        setPath(request, "/");

        Optional<String> profile = lookup.getEntity(request);

        assertFalse(profile.isPresent());
    }

    @Test
    public void should_return_none_if_base_domain_is_requested(){
        properties.setProperty(EssayistConfig.BASE_DOMAIN, "localhost");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn("localhost");
        setPath(request, "/");

        Optional<String> profile = lookup.getEntity(request);
        assertFalse(profile.isPresent());
    }

    @Test
    public void should_return_some_if_entity_in_path(){
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn("localhost");
        setPath(request, "/uns0b.tent.is/essay/2QpItzoWxwS3OxMd4Mjg1A");


        Optional<String> profile = lookup.getEntity(request);

        assertEquals("https://uns0b.tent.is", profile.get());
    }

    @Test
    public void should_return_some_if_entity_in_path_to_base_domain(){
        properties.setProperty(EssayistConfig.BASE_DOMAIN, "localhost");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn("localhost");
        setPath(request, "/uns0b.tent.is/essay/2QpItzoWxwS3OxMd4Mjg1A");

        Optional<String> profile = lookup.getEntity(request);

        assertEquals("https://uns0b.tent.is", profile.get());
    }

    @Test
    public void should_return_profile_if_sub_domain_is_requested(){

        User user = new User("http://pjesi.com");
        when(userService.getUserByDomain("subdomain.localhost"))
                .thenReturn(of(user));

        properties.setProperty(EssayistConfig.BASE_DOMAIN, "localhost");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn("subdomain.localhost");
        Optional<String> entity = lookup.getEntity(request);
        assertTrue(entity.isPresent());
    }

    @Test
    public void should_not_return_profile_if_sub_domain_is_unknown(){


        when(userService.getUserByDomain("subdomain.localhost"))
                .thenReturn(Optional.<User>absent());

        properties.setProperty(EssayistConfig.BASE_DOMAIN, "localhost");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn("subdomain.localhost");
        Optional<String> entity = lookup.getEntity(request);
        assertFalse(entity.isPresent());

    }

    @Test
    public void should_return_default_profile_as_fallback(){

        properties.setProperty(EssayistConfig.DEFAULT_ENTITY, "http://pjesi.com");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn("localhost");
        setPath(request, "/");


        Optional<String> profile = lookup.getEntity(request);
        assertEquals("http://pjesi.com", profile.get());


    }

    @Test
    public void should_return_default_profile_as_fallback_on_null(){

        properties.setProperty(EssayistConfig.DEFAULT_ENTITY, "http://pjesi.com");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn("localhost");
        setPath(request, "/");

        Optional<String> profile = lookup.getEntity(request);
        assertEquals("http://pjesi.com", profile.get());


    }

    @Test
    public void should_return_default_profile_as_fallback_on_essays(){

        properties.setProperty(EssayistConfig.DEFAULT_ENTITY, "http://pjesi.com");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn("localhost");
        setPath(request, "/essays");


        Optional<String> profile = lookup.getEntity(request);
        assertEquals("http://pjesi.com", profile.get());


    }

    @Test
    public void should_parse_essay_url_for_root(){

        properties.setProperty(EssayistConfig.DEFAULT_ENTITY, "http://pjesi.com");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn("localhost");

        setPath(request, "/essay/IpGExQrfkZkKOdMCb4nLHQ");


        TentRequest tent = lookup.getTentRequest(request).get();
        assertEquals("IpGExQrfkZkKOdMCb4nLHQ", tent.getPost());
        assertNull(tent.getAction());

    }

    @Test
    public void should_parse_essay_url_for_root_with_action(){

        properties.setProperty(EssayistConfig.DEFAULT_ENTITY, "http://pjesi.com");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn("localhost");

        setPath(request, "/essay/IpGExQrfkZkKOdMCb4nLHQ/favorite");


        TentRequest tent = lookup.getTentRequest(request).get();
        assertEquals("IpGExQrfkZkKOdMCb4nLHQ", tent.getPost());
        assertEquals("favorite", tent.getAction());

    }

    @Test
    public void should_parse_essay_url_for_entity(){

        properties.setProperty(EssayistConfig.BASE_DOMAIN, "localhost");


        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn("localhost");

        setPath(request, "/uns0b.tent.is/essay/2QpItzoWxwS3OxMd4Mjg1A");


        TentRequest tent = lookup.getTentRequest(request).get();
        assertEquals("https://uns0b.tent.is", tent.getEntity());
        assertEquals("2QpItzoWxwS3OxMd4Mjg1A", tent.getPost());
        assertNull(tent.getAction());

    }

    @Test
    public void should_parse_essay_url_for_entity_with_action(){

        properties.setProperty(EssayistConfig.BASE_DOMAIN, "localhost");


        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn("localhost");

        setPath(request, "/uns0b.tent.is/essay/2QpItzoWxwS3OxMd4Mjg1A/favorite");


        TentRequest tent = lookup.getTentRequest(request).get();
        assertEquals("https://uns0b.tent.is", tent.getEntity());
        assertEquals("2QpItzoWxwS3OxMd4Mjg1A", tent.getPost());
        assertEquals("favorite", tent.getAction());

    }

    @Test
    @Ignore // irrelevant when using requestURI
    public void should_parse_essay_url_for_short_pathinfo(){

        properties.setProperty(EssayistConfig.DEFAULT_ENTITY, "http://pjesi.com");


        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn("pjesi.com");

        setPath(request, "/2QpItzoWxwS3OxMd4Mjg1A");

        TentRequest tent = lookup.getTentRequest(request).get();
        assertEquals("http://pjesi.com", tent.getEntity());
        assertEquals("2QpItzoWxwS3OxMd4Mjg1A", tent.getPost());

    }



}
