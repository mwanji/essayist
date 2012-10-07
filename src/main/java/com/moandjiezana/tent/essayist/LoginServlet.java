package com.moandjiezana.tent.essayist;

import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.apps.AuthorizationRequest;
import com.moandjiezana.tent.client.apps.RegistrationRequest;
import com.moandjiezana.tent.client.apps.RegistrationResponse;
import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.essayist.auth.AuthResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class LoginServlet extends HttpServlet {
  
  private final Users users;

  @Inject
  public LoginServlet(Users users) {
    this.users = users;
  }
  
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException ,IOException {
    new LoginTemplate().render(resp.getWriter());
  };

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String entity = req.getParameter("entity");
    
    User user = users.getByEntityOrNull(entity);
    if (user != null) {
      req.getSession().setAttribute(User.class.getName(), user);
      resp.sendRedirect("/essays");
      
      return;
    }
    
    TentClient tentClient = new TentClient(entity);
    tentClient.discover();
    Profile profile = tentClient.getProfile();
    
    Map<String, String> scopes = new HashMap<>();
    scopes.put("write_posts", "Will post Essays and optionally Statuses");
    
    String redirectUrl = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + req.getContextPath() + "/accessToken";
    RegistrationRequest registrationRequest = new RegistrationRequest("Essayist", "A blogging app.", "http://www.moandjiezana.com/tent/essayist", new String [] { redirectUrl }, scopes);
    RegistrationResponse registrationResponse = tentClient.register(registrationRequest);
    
    AuthResult authResult = new AuthResult();
    authResult.profile = profile;
    authResult.registrationResponse = registrationResponse;

    AuthorizationRequest authorizationRequest = new AuthorizationRequest(registrationResponse.getMacKeyId(), registrationRequest.getRedirectUris()[0]);
    authorizationRequest.setScope("write_posts", "read_posts");
    authorizationRequest.setTentPostTypes("https://tent.io/types/post/essay/v0.1.0", "https://tent.io/types/post/status/v0.1.0");
    authorizationRequest.setState(UUID.randomUUID().toString());
    String authorizationUrl = tentClient.buildAuthorizationUrl(registrationResponse, authorizationRequest);
    
    getServletContext().setAttribute(authorizationRequest.getState(), authResult);
    resp.sendRedirect(authorizationUrl);
  }
}
