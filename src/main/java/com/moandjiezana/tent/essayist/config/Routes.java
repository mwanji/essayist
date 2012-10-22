package com.moandjiezana.tent.essayist.config;

import com.google.inject.servlet.RequestScoped;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.essayist.tent.Entities;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@RequestScoped
public class Routes {

  private final HttpServletRequest req;

  @Inject
  public Routes(HttpServletRequest req) {
    this.req = req;
  }
  
  public String essay(Post essay) {
    return req.getContextPath() + "/" + Entities.getForUrl(essay.getEntity()) + "/essay/" + essay.getId();
  }
  
  public String comment(Post essay) {
    return essay(essay) + "/comment";
  }
}
