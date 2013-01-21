package com.moandjiezana.tent.essayist.config;

import com.google.common.base.Splitter;
import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.essayist.EssayistSession;
import com.moandjiezana.tent.essayist.User;
import com.moandjiezana.tent.essayist.security.Csrf;
import com.moandjiezana.tent.essayist.text.TextTransformation;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

public class JamonContext {

  private static final Splitter SLASH = Splitter.on('/').omitEmptyStrings();
  
  public final String contextPath;
  public final Routes routes;
  public final Csrf csrf = new Csrf();
  public final TextTransformation textTransformation;
  
  private final HttpServletRequest req;
  public final String currentUrl;
  private final EssayistSession session;
  private final EssayistConfig config;
  private final EntityLookup entityLookup;
  
  @Inject
  public JamonContext(EssayistSession session, TextTransformation textTransformation,
                      Routes routes, HttpServletRequest req, EssayistConfig config,
                      EntityLookup entityLookup) {
    this.session = session;
    this.textTransformation = textTransformation;
    this.routes = routes;
    this.req = req;
    this.config = config;
    this.entityLookup = entityLookup;
    this.contextPath = req.getContextPath();
    this.currentUrl = req.getRequestURL().toString();
  }
  
  public boolean isLoggedIn() {
    return session.isLoggedIn();
  }
  
  public Profile getSessionProfile() {
    return getCurrentUser().getProfile();
  }
  
  public User getCurrentUser() {
    return session.getUser();
  }
  
  public String contextPath() {
    return contextPath == null || contextPath.isEmpty() ? "/" : contextPath;
  }

  public String getEssaysUrl(String entity){
      StringBuilder builder = new StringBuilder(contextPath());

      if(!config.isDefaultEntity(entity)){
          builder.append("/");
          builder.append(entity);
      }

      builder.append("/essays");

      return builder.toString();
  }
  
  public String getLastPathSegment() {
    String path = "";
    for (String part : SLASH.split(req.getRequestURI())) {
      path = part;
    }

    return path;
  }

  public EssayistConfig getConfig(){
      return config;
  }
}
