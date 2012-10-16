package com.moandjiezana.tent.essayist;

import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.apps.AuthorizationRequest;
import com.moandjiezana.tent.client.apps.RegistrationRequest;
import com.moandjiezana.tent.client.apps.RegistrationResponse;
import com.moandjiezana.tent.client.posts.Post;
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
  private final Templates templates;

  @Inject
  public LoginServlet(Users users, Templates jamonContext) {
    this.users = users;
    this.templates = jamonContext;
  }
  
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException ,IOException {

    if (req.getSession(false) != null) {
      User user = (User) req.getSession().getAttribute(User.class.getName());
      
      if (user == null) {
        AuthResult authResult = (AuthResult) req.getSession().getAttribute(req.getParameter("state"));
        if (authResult != null) {
          user = users.getByEntityOrNull(authResult.profile.getCore().getEntity());
          req.getSession().setAttribute(User.class.getName(), user);
          req.getSession().removeAttribute("state");
        }
      }
      
      if (user != null) {
        resp.sendRedirect(req.getContextPath() + "/read");
        
        return;
      }
    }

    templates.login().render(resp.getWriter());
  };

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String entity = req.getParameter("entity");
    if (entity.endsWith("/")) {
      entity = entity.substring(0, entity.length() - 1);
    }
    User user = users.getByEntityOrNull(entity);
    
    TentClient tentClient;
    RegistrationResponse registrationResponse;
    String redirectUri;
    
    if (user != null && user.getRegistration() != null) {
      tentClient = new TentClient(user.getProfile());
      tentClient.getAsync().setAccessToken(user.getAccessToken());
      tentClient.getAsync().setRegistrationResponse(user.getRegistration());
      redirectUri = user.getRegistration().getRedirectUris()[1];
      registrationResponse = user.getRegistration();
    } else {
      tentClient = new TentClient(entity);
      tentClient.getProfile();
      
      Map<String, String> scopes = new HashMap<String, String>();
      scopes.put("write_posts", "Will post Essays and optionally Statuses");
      
      String baseUrl = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + req.getContextPath();
      String afterAuthorizationUrl = baseUrl + "/accessToken";
      String afterLoginUrl = baseUrl;
      
      RegistrationRequest registrationRequest = new RegistrationRequest("Essayist", "A blogging app.", "http://www.moandjiezana.com/tent/essayist", new String [] { afterAuthorizationUrl, afterLoginUrl }, scopes);
      registrationResponse = tentClient.register(registrationRequest);
      redirectUri = registrationResponse.getRedirectUris()[0];
    }

    AuthorizationRequest authorizationRequest = new AuthorizationRequest(registrationResponse.getMacKeyId(), redirectUri);
    authorizationRequest.setScope("write_posts", "read_posts");
    authorizationRequest.setTentPostTypes(Post.Types.essay("v0.1.0"), Post.Types.status("v0.1.0"), Post.Types.photo("v0.1.0"));
    authorizationRequest.setState(UUID.randomUUID().toString());
    String authorizationUrl = tentClient.buildAuthorizationUrl(authorizationRequest);
    
    AuthResult authResult = new AuthResult();
    authResult.profile = tentClient.getProfile();
    authResult.registrationResponse = registrationResponse;
    req.getSession().setAttribute(authorizationRequest.getState(), authResult);
    
    resp.sendRedirect(authorizationUrl);
  }
}
