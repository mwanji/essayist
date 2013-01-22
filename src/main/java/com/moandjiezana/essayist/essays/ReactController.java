package com.moandjiezana.essayist.essays;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import co.mewf.merf.Response;
import co.mewf.merf.http.GET;
import co.mewf.merf.http.POST;
import co.mewf.merf.http.Responses;
import co.mewf.merf.http.Url;

import com.google.common.base.Strings;
import com.moandjiezana.essayist.auth.Authenticated;
import com.moandjiezana.essayist.merf.JamonResponse;
import com.moandjiezana.essayist.sessions.EssayistSession;
import com.moandjiezana.essayist.tent.posts.Bookmark;
import com.moandjiezana.essayist.tent.posts.Favorite;
import com.moandjiezana.essayist.users.Entities;
import com.moandjiezana.essayist.users.User;
import com.moandjiezana.essayist.users.Users;
import com.moandjiezana.essayist.views.Routes;
import com.moandjiezana.essayist.views.Templates;
import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Mention;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.posts.PostQuery;
import com.moandjiezana.tent.client.posts.content.Repost;
import com.moandjiezana.tent.client.posts.content.StatusContent;
import com.moandjiezana.tent.client.users.Permissions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@Url("/{authorEntity}/essay/{postId}")
public class ReactController {

  private final Users users;
  private final Templates templates;
  private final Routes routes;
  private final EssayistSession session;
  private final HttpServletRequest req;

  @Inject
  public ReactController(Users users, Templates templates, Routes routes, EssayistSession session, HttpServletRequest req) {
    this.users = users;
    this.templates = templates;
    this.routes = routes;
    this.session = session;
    this.req = req;
  }

  @GET @Url("/reactions")
  public JamonResponse getReactions(String authorEntity, String postId) {
    String fullAuthorEntity = Entities.expandFromUrl(authorEntity);
    User user = users.getByEntityOrNull(fullAuthorEntity);
    TentClient tentClient;
    if (user != null) {
      tentClient = new TentClient(user.getProfile());
      tentClient.getAsync().setAccessToken(user.getAccessToken());
      tentClient.getAsync().setRegistrationResponse(user.getRegistration());
    } else {
      tentClient = new TentClient(fullAuthorEntity);
      tentClient.getProfile();
    }
    //.postTypes(Bookmark.URI, Favorite.URI, Post.Types.repost("v0.1.0"), Post.Types.status("v0.1.0"))
    List<Post> reactions = tentClient.getPosts(new PostQuery().mentionedPost(postId));

    return new JamonResponse(templates.reactions().makeRenderer(reactions));
  }

  @Authenticated
  @POST @Url("/{action}")
  public Response react(String authorEntity, String postId, String action) throws MalformedURLException {
    String fullAuthorEntity = Entities.expandFromUrl(authorEntity);

    User user = session.getUser();
    TentClient tentClient = new TentClient(user.getProfile());
    tentClient.getAsync().setAccessToken(user.getAccessToken());
    tentClient.getAsync().setRegistrationResponse(user.getRegistration());

    Post post = new Post();
    post.setEntity(user.getProfile().getCore().getEntity());
    post.setMentions(new Mention[] { new Mention(fullAuthorEntity, postId) });
    Permissions permissions = new Permissions();
    permissions.setPublic(true);
    post.setPermissions(permissions);
    post.setLicenses(new String[] { "http://creativecommons.org/licenses/by/3.0/" });

    Post essay = new Post();
    essay.setEntity(fullAuthorEntity);
    essay.setId(postId);

    if ("status".equals(action)) {
      String commentText = req.getParameter("comment");

      if (Strings.isNullOrEmpty(commentText)) {
        return Responses.redirect(routes.essay(essay));
      }

      commentText = commentText.substring(0, Math.min(commentText.length(), 256));
      post.setContent(new StatusContent(commentText));
    } else if ("favorite".equals(action)) {
      post.setContent(new Favorite(authorEntity, postId));
    } else if ("bookmark".equals(action)) {
      String requestUrl = req.getRequestURL().toString();
      Bookmark bookmark = new Bookmark(new URL(requestUrl.substring(0, requestUrl.lastIndexOf('/'))), req.getParameter("title"));
      bookmark.setDescription(req.getParameter("description"));
      bookmark.setSiteName(req.getParameter("name") + " on Essayist");
      post.setContent(bookmark);
    } else if ("repost".equals(action)) {
      post.setContent(new Repost(authorEntity, postId));
    } else {
      return Responses.status(SC_BAD_REQUEST);
    }

    tentClient.write(post);

    return Responses.redirect(routes.essay(essay));
  }
}
