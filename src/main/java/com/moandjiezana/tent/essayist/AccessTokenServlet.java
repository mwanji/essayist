package com.moandjiezana.tent.essayist;

import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.essayist.auth.AuthResult;
import com.moandjiezana.tent.oauth.AccessToken;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class AccessTokenServlet extends HttpServlet {
  
  private Users users;
  private Provider<EssayistSession> sessions;

  @Inject
  public AccessTokenServlet(Users users, Provider<EssayistSession> sessions) {
    this.users = users;
    this.sessions = sessions;
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
    TentClient tentClient = new TentClient(profile);
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
    
    sessions.get().setUser(user);
    resp.sendRedirect(req.getContextPath() + "/read");
  }
}
