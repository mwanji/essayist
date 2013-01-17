package com.moandjiezana.tent.essayist;

import com.google.inject.servlet.SessionScoped;
import com.moandjiezana.tent.client.posts.Post;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

@SessionScoped
public class EssayistSession {
  
  private static final User LOGGED_OUT = new User() {
    public boolean owns(Post post) {
      return false;
    };
  };

  private User user = LOGGED_OUT;

  private HttpSession httpSession;
  
  @Inject
  public EssayistSession(HttpSession httpSession) {
    this.httpSession = httpSession;
  }
  
  public boolean isLoggedIn() {
    return getUser() != LOGGED_OUT;
  }

  public boolean isEntity(String entity){
    return isLoggedIn() && entity.equals(getUser().getProfile().getCore().getEntity());
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user != null ? user : LOGGED_OUT;
    this.httpSession.setAttribute(EssayistSession.class.getName(), Boolean.valueOf(user != LOGGED_OUT));
  }
}
