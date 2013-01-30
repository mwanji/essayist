package com.moandjiezana.tent.essayist;

import com.google.common.base.Throwables;
import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.posts.PostQuery;
import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.essayist.config.EntityLookup;
import com.moandjiezana.tent.essayist.config.TentRequest;
import com.moandjiezana.tent.essayist.security.Csrf;
import com.moandjiezana.tent.essayist.tent.Entities;
import com.moandjiezana.tent.essayist.tent.EssayistPostContent;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class EssayServlet extends HttpServlet {

  private final Templates templates;
  private final Users users;
  private final Provider<EssayistSession> sessions;
  private final Csrf csrf;
  private final EntityLookup entityLookup;


  @Inject
  public EssayServlet(Users users, Provider<EssayistSession> sessions,
                      Templates templates, Csrf csrf, EntityLookup entityLookup) {
    this.users = users;
    this.sessions = sessions;
    this.templates = templates;
    this.csrf = csrf;
    this.entityLookup = entityLookup;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    TentRequest tentRequest = entityLookup.getTentRequest(req)
            .get(); // TODO 404 if not found



    String authorEntity = tentRequest.getEntity();
    String essayId = tentRequest.getPost();


    User author = users.getByEntityOrNull(authorEntity);

    TentClient tentClient;

    User user = sessions.get().getUser();
    /*if (sessions.get().isLoggedIn()) {
      tentClient = new TentClient(user.getProfile());
      tentClient.getAsync().setAccessToken(user.getAccessToken());
      tentClient.getAsync().setRegistrationResponse(user.getRegistration());
    } else */if (author != null) {
      tentClient = new TentClient(author.getProfile());
    } else {
      tentClient = new TentClient(authorEntity);
      tentClient.discover();
      Profile profile = tentClient.getProfile();
      author = new User(profile);
      users.save(author);
    }

    Post post = tentClient.getPost(essayId);

    EssayistPostContent essayContent = post.getContentAs(EssayistPostContent.class);
    essayContent.setBody(csrf.permissive(essayContent.getBody()));

    EssayPage essayPage = templates.essay();
    if (user.owns(post)) {
      essayPage.setActive("Written");
    }

    if (Boolean.parseBoolean(req.getParameter("reactions"))) {
      tentClient.getAsync().setAccessToken(author.getAccessToken());
      tentClient.getAsync().setRegistrationResponse(author.getRegistration());
      List<Post> reactions = tentClient.getPosts(new PostQuery().mentionedPost(essayId));
      essayPage.setReactions(reactions);
    }

    essayPage.render(resp.getWriter(), post, author.getProfile());
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String[] parts = entityLookup.getRequestPath(req).split("/essay/");
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

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

  }
}
