package com.moandjiezana.tent.essayist;

import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.users.Permissions;
import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.essayist.auth.AuthResult;
import com.moandjiezana.tent.oauth.AccessToken;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class AccessTokenServlet extends HttpServlet {
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    AuthResult authResult = (AuthResult) getServletContext().getAttribute(req.getParameter("state"));
    
    Profile profile = authResult.profile;
    TentClient tentClient = new TentClient(profile, Collections.<String>emptyList());
    AccessToken accessToken = tentClient.getAccessToken(authResult.registrationResponse, req.getParameter("code"));
    
    Post post = new Post();
    post.setType("https://tent.io/types/post/status/v0.1.0");
    post.setPublishedAt(System.currentTimeMillis() / 1000);
    Permissions permissions = new Permissions();
    permissions.setPublicVisible(true);
    post.setPermissions(permissions);
    post.setLicenses(new String[] { "http://creativecommons.org/licenses/by/3.0/" });
    HashMap<String, Object> content = new HashMap<String, Object>();
    content.put("text", "Essayist is installed");
    post.setContent(content);

    tentClient.getAsync().write(post);
    
    req.getSession().setAttribute(User.class.getName(), new User(profile, accessToken));
    resp.sendRedirect(req.getContextPath() + "/essays");
  }
}
