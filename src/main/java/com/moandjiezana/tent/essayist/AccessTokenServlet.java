package com.moandjiezana.tent.essayist;

import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.posts.content.StatusContent;
import com.moandjiezana.tent.client.users.Permissions;
import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.essayist.auth.AuthResult;
import com.moandjiezana.tent.essayist.tent.Entities;
import com.moandjiezana.tent.oauth.AccessToken;

import java.io.IOException;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class AccessTokenServlet extends HttpServlet {
  
  private Users users;

  @Inject
  public AccessTokenServlet(Users users) {
    this.users = users;
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    AuthResult authResult = (AuthResult) req.getSession().getAttribute(req.getParameter("state"));
    req.getSession().removeAttribute("state");
    
    if (authResult == null) {
      resp.sendError(HttpServletResponse.SC_FORBIDDEN, "No corresponding authentication request found.");
     
      return;
    }
    
    Profile profile = authResult.profile;
    TentClient tentClient = new TentClient(profile, Collections.<String>emptyList());
    tentClient.getAsync().setRegistrationResponse(authResult.registrationResponse);
    AccessToken accessToken = tentClient.getAccessToken(authResult.registrationResponse, req.getParameter("code"));
    
    User existingUser = users.getByEntityOrNull(profile.getCore().getEntity());
    User user;
    if (existingUser != null) {
      user = new User(existingUser.getId(), profile, authResult.registrationResponse, accessToken);
    } else {
      user = new User(profile, authResult.registrationResponse, accessToken);
    }
    
    users.save(user);
    
    Post post = new Post();
    post.setPublishedAt(System.currentTimeMillis() / 1000);
    Permissions permissions = new Permissions();
    permissions.setPublic(true);
    post.setPermissions(permissions);
    post.setLicenses(new String[] { "http://creativecommons.org/licenses/by/3.0/" });
    StatusContent status = new StatusContent();
    status.setText("Essayist is installed");
    post.setContent(status);

    tentClient.write(post);
    
    req.getSession().setAttribute(User.class.getName(), user);
    resp.sendRedirect(req.getContextPath() + "/" + Entities.getEntityForUrl(profile.getCore().getEntity()) + "/essays");
  }
}
