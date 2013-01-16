package com.moandjiezana.essayist.sessions;

import com.moandjiezana.tent.essayist.User;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

public class EssayistSession {
  private HttpSession httpSession;

  @Inject
  public EssayistSession(HttpSession httpSession) {
    this.httpSession = httpSession;
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
}
