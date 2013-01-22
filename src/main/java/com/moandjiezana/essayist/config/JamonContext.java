package com.moandjiezana.essayist.config;

import com.google.common.base.Splitter;
import com.moandjiezana.essayist.security.Csrf;
import com.moandjiezana.essayist.sessions.EssayistSession;
import com.moandjiezana.essayist.text.TextTransformation;
import com.moandjiezana.essayist.users.User;
import com.moandjiezana.essayist.views.Routes;
import com.moandjiezana.tent.client.users.Profile;

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

  @Inject
  public JamonContext(EssayistSession session, TextTransformation textTransformation, Routes routes, HttpServletRequest req) {
    this.session = session;
    this.textTransformation = textTransformation;
    this.routes = routes;
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
