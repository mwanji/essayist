package com.moandjiezana.tent.essayist;

import com.google.common.base.Throwables;
import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.essayist.tent.Entities;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class EssayServlet extends HttpServlet {
  
  private Templates templates;
  private Users users;
  private Provider<EssayistSession> sessions;

  @Inject
  public EssayServlet(Users users, Provider<EssayistSession> sessions, Templates templates) {
    this.users = users;
    this.sessions = sessions;
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
  
  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String[] parts = req.getPathInfo().split("/essay/");
    String authorEntity = parts[0];
    String fullAuthorEntity = Entities.expandFromUrl(authorEntity);
    
    User user = sessions.get().getUser();
    
    if (!fullAuthorEntity.equals(user.getProfile().getCore().getEntity())) {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not permitted to delete this Essay.");
      return;
    }
    
    TentClient tentClient = new TentClient(user.getProfile());
    tentClient.getAsync().setAccessToken(user.getAccessToken());
    tentClient.getAsync().setRegistrationResponse(user.getRegistration());

    try {
      tentClient.getAsync().deletePost(parts[1]).get();
    } catch (Exception e) {
      Throwables.propagate(Throwables.getRootCause(e));
    }
    
    resp.sendRedirect(req.getContextPath() + "/" + authorEntity + "/essays");
  }
}
