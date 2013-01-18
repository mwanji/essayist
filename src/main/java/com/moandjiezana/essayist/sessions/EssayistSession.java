package com.moandjiezana.essayist.sessions;

import com.moandjiezana.tent.essayist.User;
import com.moandjiezana.tent.essayist.auth.AuthResult;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class EssayistSession {
  private HttpSession httpSession;
  private HttpServletRequest request;

  @Inject
  public EssayistSession(HttpSession httpSession, HttpServletRequest request) {
    this.httpSession = httpSession;
    this.request = request;
  }

  public boolean isLoggedIn() {
    return httpSession != null && httpSession.getAttribute(User.class.getName()) != null;
  }

  public User getUser() {
    return (User) httpSession.getAttribute(User.class.getName());
  }

  public void setUser(User user) {
    httpSession.setAttribute(User.class.getName(), user);
  }

  public AuthResult consumeAuthResult() {
    AuthResult authResult = (AuthResult) httpSession.getAttribute(request.getParameter("state"));
    httpSession.removeAttribute("state");
    return authResult;
  }
}
