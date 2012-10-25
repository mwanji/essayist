package com.moandjiezana.tent.essayist;

import com.google.common.base.Strings;
import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Mention;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.posts.content.StatusContent;
import com.moandjiezana.tent.client.users.Permissions;
import com.moandjiezana.tent.essayist.auth.Authenticated;
import com.moandjiezana.tent.essayist.config.Routes;
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
@Authenticated
public class CommentsServlet extends HttpServlet {
  
  private final Provider<EssayistSession> sessions;
  private final Provider<Routes> routes;

  @Inject
  public CommentsServlet(Provider<Routes> routes, Provider<EssayistSession> sessions) {
    this.routes = routes;
    this.sessions = sessions;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String[] parts = req.getPathInfo().split("/essay/");
    String authorEntity = Entities.expandFromUrl(parts[0]);
    String essayId = parts[1].split("/")[0];

    String commentText = req.getParameter("comment");
    
    Post essay = new Post();
    essay.setEntity(authorEntity);
    essay.setId(essayId);
    
    if (Strings.isNullOrEmpty(commentText)) {
      resp.sendRedirect(routes.get().essay(essay));
      
      return;
    }

    User user = sessions.get().getUser();
    TentClient tentClient = new TentClient(user.getProfile());
    tentClient.getAsync().setAccessToken(user.getAccessToken());
    tentClient.getAsync().setRegistrationResponse(user.getRegistration());
    
    Post comment = new Post();
    comment.setEntity(user.getProfile().getCore().getEntity());
    commentText = commentText.substring(0, Math.min(commentText.length(), 256));
    comment.setContent(new StatusContent(commentText));
    comment.setMentions(new Mention[] { new Mention(authorEntity, essayId) });
    Permissions permissions = new Permissions();
    permissions.setPublic(true);
    comment.setPermissions(permissions);
    comment.setLicenses(new String[] { "http://creativecommons.org/licenses/by/3.0/" });
    
    tentClient.write(comment);
    
    resp.sendRedirect(routes.get().essay(essay));
  }
}
