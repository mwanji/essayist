package com.moandjiezana.tent.essayist;

import com.google.inject.servlet.SessionScoped;
import com.moandjiezana.tent.client.posts.Post;

@SessionScoped
public class EssayistSession {
  
  private static final User LOGGED_OUT = new User() {
    public boolean owns(Post post) {
      return false;
    };
  };

  private User user = LOGGED_OUT;
  
  public boolean isLoggedIn() {
    return getUser() != LOGGED_OUT;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }
}
