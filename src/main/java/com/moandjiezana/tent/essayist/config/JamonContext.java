package com.moandjiezana.tent.essayist.config;

import com.google.common.base.Splitter;
import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.essayist.EssayistSession;
import com.moandjiezana.tent.essayist.User;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

public class JamonContext {

  private static final Splitter SLASH = Splitter.on('/').omitEmptyStrings();
  
  private final HttpServletRequest req;
  public final String contextPath;
  public final String currentUrl;
  private EssayistSession session;
  
  @Inject
  public JamonContext(EssayistSession session, HttpServletRequest req) {
    this.session = session;
    this.req = req;
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
  
  public String getLastPathSegment() {
    String path = "";
    for (String part : SLASH.split(req.getRequestURI())) {
      path = part;
    }

    return path;
  }
}
