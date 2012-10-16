package com.moandjiezana.tent.essayist;

import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.essayist.tent.Entities;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class EssayServlet extends HttpServlet {
  
  private Templates templates;
  private Users users;

  @Inject
  public EssayServlet(Users users, Templates templates) {
    this.users = users;
    this.templates = templates;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String[] parts = req.getPathInfo().split("/essay/");
    String authorEntity = Entities.expandFromUrl(parts[0]);
    String essayId = parts[1];
    
    User user = users.getByEntityOrNull(authorEntity);

    TentClient authorTentClient;
    if (user != null) {
      authorTentClient = new TentClient(user.getProfile());
    } else {
      authorTentClient = new TentClient(authorEntity);
      authorTentClient.discover();
      Profile profile = authorTentClient.getProfile();
      user = new User(profile, null);
      users.save(user);
    }
    
    Post post = authorTentClient.getPost(essayId);
    
    templates.essay().render(resp.getWriter(), post, user.getProfile());
  }
}
