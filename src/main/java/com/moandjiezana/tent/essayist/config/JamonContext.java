package com.moandjiezana.tent.essayist.config;

import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.essayist.User;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class JamonContext {

  private final HttpServletRequest req;
  public final String contextPath;
  
  @Inject
  public JamonContext(HttpServletRequest req) {
    this.req = req;
    this.contextPath = req.getContextPath();
  }
  
  public boolean isLoggedIn() {
    HttpSession session = req.getSession(false);
    
    if (session == null) {
      return false;
    }
    
    return session.getAttribute(User.class.getName()) != null;
  }
  
  public Profile getSessionProfile() {
    return ((User) req.getSession(false).getAttribute(User.class.getName())).getProfile();
  }
  
  public String contextPath() {
    return contextPath == null || contextPath.isEmpty() ? "/" : contextPath;
  }
}
