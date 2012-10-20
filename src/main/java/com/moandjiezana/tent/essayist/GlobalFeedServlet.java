package com.moandjiezana.tent.essayist;

import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.essayist.tent.Entities;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class GlobalFeedServlet extends HttpServlet {
  
  private final Templates templates;
  private Essays essays;
  private Users users;

  @Inject
  public GlobalFeedServlet(Users users, Essays essays, Templates templates) {
    this.users = users;
    this.essays = essays;
    this.templates = templates;
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    User user = (User) req.getSession().getAttribute(User.class.getName());
    List<Post> essays = Collections.emptyList();
    
//    TentClient tentClient = new TentClient(user.getProfile());
//    tentClient.getAsync().setAccessToken(user.getAccessToken());
//    tentClient.getAsync().setRegistrationResponse(user.getRegistration());
    List<User> allUsers = users.getAll();
    essays = this.essays.getEssays(allUsers);
    
    Map<String, Profile> profiles = new HashMap<String, Profile>();
    for (User aUser : allUsers) {
      profiles.put(aUser.getProfile().getCore().getEntity(), aUser.getProfile());
    }
    
    templates.read().setEssays(essays).render(resp.getWriter(), profiles);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.sendRedirect(req.getContextPath() + "/" + Entities.getForUrl(req.getParameter("entity")) + "/essays");
  }
}
