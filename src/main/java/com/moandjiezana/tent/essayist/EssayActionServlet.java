package com.moandjiezana.tent.essayist;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.moandjiezana.essayist.posts.Bookmark;
import com.moandjiezana.essayist.posts.Favorite;
import com.moandjiezana.essayist.posts.UserReactions;
import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Mention;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.posts.PostQuery;
import com.moandjiezana.tent.client.posts.content.Repost;
import com.moandjiezana.tent.client.posts.content.StatusContent;
import com.moandjiezana.tent.client.users.Permissions;
import com.moandjiezana.tent.essayist.auth.Authenticated;
import com.moandjiezana.tent.essayist.config.EntityLookup;
import com.moandjiezana.tent.essayist.config.Routes;
import com.moandjiezana.tent.essayist.config.TentRequest;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class EssayActionServlet extends HttpServlet {

  private final Provider<EssayistSession> sessions;
  private final Provider<Routes> routes;
  private Users users;
  private Templates templates;
  private final EntityLookup entityLookup;


    @Inject
  public EssayActionServlet(Users users, Provider<Routes> routes,
                            Provider<EssayistSession> sessions,
                            Templates templates, EntityLookup entityLookup) {
    this.users = users;
    this.routes = routes;
    this.sessions = sessions;
    this.templates = templates;
    this.entityLookup = entityLookup;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
          throws ServletException, IOException {

    TentRequest tentRequest = entityLookup.getTentRequest(req)
              .get(); // TODO 404 if not found

    String authorEntity = req.getParameter("entity") != null ? req.getParameter("entity") :
            tentRequest.getEntity();

    String postId = tentRequest.getPost();
    String action = tentRequest.getAction();


    if ("user".equals(action)) {
      User loggedUser = sessions.get().getUser();
      String loggedEntity = loggedUser.getProfile().getCore().getEntity();
      TentClient tentClient = new TentClient(loggedUser.getProfile());
      List<Post> reactions = tentClient.getPosts(new PostQuery().mentionedPost(postId).entity(loggedEntity).postTypes(Bookmark.URI, Favorite.URI, Post.Types.repost("v0.1.0")));
      UserReactions userReactions = new UserReactions();
      for (Post reaction : reactions) {
        String reactionType = reaction.getType();
        if (Post.Types.equalsIgnoreVersion(Bookmark.URI, reactionType)) {
          userReactions.bookmarked = true;
        } else if (Post.Types.equalsIgnoreVersion(Favorite.URI, reactionType)) {
          userReactions.favorited = true;
        }
      }
    } else if ("reactions".equals(action)) {
      User user = users.getByEntityOrNull(authorEntity);
      TentClient tentClient;
      if (user != null) {
        tentClient = new TentClient(user.getProfile());
        tentClient.getAsync().setAccessToken(user.getAccessToken());
        tentClient.getAsync().setRegistrationResponse(user.getRegistration());
      } else {
        tentClient = new TentClient(authorEntity);
        tentClient.getProfile();
      }
      //.postTypes(Bookmark.URI, Favorite.URI, Post.Types.repost("v0.1.0"), Post.Types.status("v0.1.0"))
      List<Post> reactions = tentClient.getPosts(new PostQuery().mentionedPost(postId));
      templates.reactions().render(resp.getWriter(), reactions);
    }
  }

  @Override
  @Authenticated
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String[] entityAndId = getEntityAndIdAndAction(req);
    String authorEntity = entityAndId[0];
    String essayId = entityAndId[1];
    String action = entityAndId[2];

    Post essay = new Post();
    essay.setEntity(authorEntity);
    essay.setId(essayId);

    User user = sessions.get().getUser();
    TentClient tentClient = new TentClient(user.getProfile());
    tentClient.getAsync().setAccessToken(user.getAccessToken());
    tentClient.getAsync().setRegistrationResponse(user.getRegistration());

    Post post = new Post();
    post.setEntity(user.getProfile().getCore().getEntity());
    post.setMentions(new Mention[] { new Mention(authorEntity, essayId) });
    Permissions permissions = new Permissions();
    permissions.setPublic(true);
    post.setPermissions(permissions);
    post.setLicenses(new String[] { "http://creativecommons.org/licenses/by/3.0/" });

    if ("status".equals(action)) {
      String commentText = req.getParameter("comment");

      if (Strings.isNullOrEmpty(commentText)) {
        resp.sendRedirect(routes.get().essay(essay));

        return;
      }

      commentText = commentText.substring(0, Math.min(commentText.length(), 256));
      post.setContent(new StatusContent(commentText));
    } else if ("favorite".equals(action)) {
      post.setContent(new Favorite(authorEntity, essayId));
    } else if ("bookmark".equals(action)) {
      String requestUrl = req.getRequestURL().toString();
      Bookmark bookmark = new Bookmark(new URL(requestUrl.substring(0, requestUrl.lastIndexOf('/'))), req.getParameter("title"));
      bookmark.setDescription(req.getParameter("description"));
      bookmark.setSiteName(req.getParameter("name") + " on Essayist");
      post.setContent(bookmark);
    } else if ("repost".equals(action)) {
      post.setContent(new Repost(authorEntity, essayId));
    }

    tentClient.write(post);

    resp.sendRedirect(routes.get().essay(essay));
  }

  private String[] getEntityAndIdAndAction(HttpServletRequest req) {
    String[] parts = entityLookup.getRequestPath(req).split("/");
    Optional<String> optionalEntity = entityLookup.getEntity(req);
    // TODO 404 if not found

    String authorEntity = optionalEntity.get();
    String essayId = parts[parts.length-2];
    String action = parts[parts.length-1];

    return new String[] { authorEntity, essayId, action };
  }
}
