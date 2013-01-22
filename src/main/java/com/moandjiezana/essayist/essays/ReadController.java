package com.moandjiezana.essayist.essays;

import co.mewf.merf.Response;
import co.mewf.merf.http.GET;
import co.mewf.merf.http.Responses;
import co.mewf.merf.http.Url;

import com.moandjiezana.essayist.auth.Authenticated;
import com.moandjiezana.essayist.merf.JamonResponse;
import com.moandjiezana.essayist.security.Csrf;
import com.moandjiezana.essayist.sessions.EssayistSession;
import com.moandjiezana.essayist.users.Entities;
import com.moandjiezana.essayist.users.User;
import com.moandjiezana.essayist.users.Users;
import com.moandjiezana.essayist.views.Routes;
import com.moandjiezana.essayist.views.Templates;
import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.posts.PostQuery;
import com.moandjiezana.tent.client.posts.content.EssayContent;
import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.essayist.EssayPage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

public class ReadController {

  private final Users users;
  private final EssayistSession session;
  private final Routes routes;
  private final Csrf csrf;
  private final Templates templates;
  private final HttpServletRequest req;
  private final Essays essays;

  @Inject
  public ReadController(Users users, Essays essays, Routes routes, EssayistSession session, Csrf csrf, Templates templates, HttpServletRequest req) {
    this.users = users;
    this.essays = essays;
    this.routes = routes;
    this.session = session;
    this.csrf = csrf;
    this.templates = templates;
    this.req = req;
  }

  @GET @Url("/{authorEntity}/essay/{essayId}")
  public Response getEssay(String authorEntity, String essayId) {
    String fullAuthorEntity = Entities.expandFromUrl(authorEntity);
    User author = users.getByEntityOrNull(fullAuthorEntity);

    TentClient tentClient;

    User user = session.getUser();
    /*
     * if (sessions.get().isLoggedIn()) { tentClient = new
     * TentClient(user.getProfile());
     * tentClient.getAsync().setAccessToken(user.getAccessToken());
     * tentClient.getAsync().setRegistrationResponse(user.getRegistration()); }
     * else
     */if (author != null) {
      tentClient = new TentClient(author.getProfile());
    } else {
      tentClient = new TentClient(fullAuthorEntity);
      tentClient.discover();
      Profile profile = tentClient.getProfile();
      author = new User(profile);
      users.save(author);
    }

    Post essay = tentClient.getPost(essayId);

    EssayContent essayContent = essay.getContentAs(EssayContent.class);
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

  @GET @Url("/{authorEntity}/essays")
  public Response getEssays(String authorEntity) {
    String fullAuthorEntity = Entities.expandFromUrl(authorEntity);
    TentClient tentClient = getTentClientFromSessionOrUrl(fullAuthorEntity);

    String active = session.getUser().isEntity(fullAuthorEntity) ? "Written" : "My Feed";

    List<Post> essays = tentClient.getPosts(new PostQuery().postTypes(Post.Types.essay("v0.1.0")).entity(fullAuthorEntity));

    return new JamonResponse(templates.essays(active).makeRenderer(essays, tentClient.getProfile()));
  }

  @Authenticated
  @GET @Url("/read")
  public Response read() {
    User user = session.getUser();

    List<Post> essaysFeed = essays.getFeed(user);
    List<User> allUsers = users.getAll();

    final Map<String, Profile> profiles = new ConcurrentHashMap<String, Profile>();

    for (User aUser : allUsers) {
      profiles.put(aUser.getProfile().getCore().getEntity(), aUser.getProfile());
    }

    for (Post essay : essaysFeed) {
      if (!profiles.containsKey(essay.getEntity())) {
        users.fetch(essay.getEntity());
      }
    }

    return new JamonResponse(templates.read().setEssays(essaysFeed).makeRenderer(profiles));
  }

  @GET @Url("/global")
  public Response global() {
    List<User> allUsers = users.getAll();
    List<Post> essays = this.essays.getEssays(allUsers);

    Map<String, Profile> profiles = new HashMap<String, Profile>();
    for (User aUser : allUsers) {
      profiles.put(aUser.getProfile().getCore().getEntity(), aUser.getProfile());
    }

    return new JamonResponse(templates.read().setEssays(essays).makeRenderer(profiles));
  }

  @GET @Url("/search/author")
  public Response findAuthor() {
    return Responses.redirect(routes.essays(Entities.getForUrl(req.getParameter("entity"))));
  }

  private TentClient getTentClientFromSessionOrUrl(String entity) {
    User user = session.getUser();

    if (!session.isLoggedIn() || !entity.equals(user.getProfile().getCore().getEntity())) {
      TentClient tentClient = new TentClient(entity);
      tentClient.getProfile();

      return tentClient;
    }

    TentClient tentClient = new TentClient(user.getProfile());
    tentClient.getAsync().setAccessToken(user.getAccessToken());

    return tentClient;
  }
}
