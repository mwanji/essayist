package com.moandjiezana.essayist.users;

import co.mewf.merf.Response;
import co.mewf.merf.http.GET;
import co.mewf.merf.http.Url;

import com.moandjiezana.essayist.merf.JamonResponse;
import com.moandjiezana.essayist.sessions.EssayistSession;
import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.posts.PostQuery;
import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.essayist.EssayPage;
import com.moandjiezana.tent.essayist.Templates;
import com.moandjiezana.tent.essayist.User;
import com.moandjiezana.tent.essayist.Users;
import com.moandjiezana.tent.essayist.security.Csrf;
import com.moandjiezana.tent.essayist.tent.Entities;
import com.moandjiezana.tent.essayist.tent.EssayistPostContent;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

public class ReadEssayController {

  private final Users users;
  private final EssayistSession session;
  private final Csrf csrf;
  private final Templates templates;
  private final HttpServletRequest req;

  @Inject
  public ReadEssayController(Users users, EssayistSession session, Csrf csrf, Templates templates, HttpServletRequest req) {
    this.users = users;
    this.session = session;
    this.csrf = csrf;
    this.templates = templates;
    this.req = req;
  }

  @GET @Url("/{authorEntity}/essay/{essayId}")
  public Response read(String authorEntity, String essayId) {
    String fullAuthorEntity = Entities.expandFromUrl(authorEntity);
    User author = users.getByEntityOrNull(fullAuthorEntity);

    TentClient tentClient;

    User user = session.getUser();
    /*if (sessions.get().isLoggedIn()) {
      tentClient = new TentClient(user.getProfile());
      tentClient.getAsync().setAccessToken(user.getAccessToken());
      tentClient.getAsync().setRegistrationResponse(user.getRegistration());
    } else */if (author != null) {
      tentClient = new TentClient(author.getProfile());
    } else {
      tentClient = new TentClient(fullAuthorEntity);
      tentClient.discover();
      Profile profile = tentClient.getProfile();
      author = new User(profile);
      users.save(author);
    }

    Post essay = tentClient.getPost(essayId);

    EssayistPostContent essayContent = essay.getContentAs(EssayistPostContent.class);
    essayContent.setBody(csrf.permissive(essayContent.getBody()));

    EssayPage essayPage = templates.essay();
    if (user.owns(essay)) {
      essayPage.setActive("Written");
    }

    if (Boolean.parseBoolean(req.getParameter("reactions"))) {
      tentClient.getAsync().setAccessToken(author.getAccessToken());
      tentClient.getAsync().setRegistrationResponse(author.getRegistration());
      List<Post> reactions = tentClient.getPosts(new PostQuery().mentionedPost(essayId));
      essayPage.setReactions(reactions);
    }

    return new JamonResponse(essayPage.makeRenderer(essay, author.getProfile()));
  }
}
