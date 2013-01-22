package com.moandjiezana.essayist.sessions;

import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.essayist.User;
import com.moandjiezana.tent.essayist.auth.AuthResult;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class EssayistSession {
  private HttpSession httpSession;
  private HttpServletRequest request;

  private static User GUEST = new User() {
    @Override
    public boolean owns(Post post) {
      return false;
    };

    @Override
    public boolean isEntity(String entity) {
      return false;
    };
  };

  @Inject
  public EssayistSession(HttpSession httpSession, HttpServletRequest request) {
    this.httpSession = httpSession;
    this.request = request;
  }

  public boolean isLoggedIn() {
    return httpSession != null && httpSession.getAttribute(User.class.getName()) != null;
  }

  public User getUser() {
    User user = (User) httpSession.getAttribute(User.class.getName());
    return user != null ? user : GUEST;
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
