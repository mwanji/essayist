package com.moandjiezana.tent.essayist;

import com.moandjiezana.essayist.posts.Bookmark;
import com.moandjiezana.essayist.posts.EssayistMetadataContent;
import com.moandjiezana.essayist.posts.Favorite;
import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.apps.AuthorizationRequest;
import com.moandjiezana.tent.client.apps.RegistrationRequest;
import com.moandjiezana.tent.client.apps.RegistrationResponse;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.essayist.auth.AuthResult;
import com.moandjiezana.tent.essayist.config.EssayistConfig;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class LoginServlet extends HttpServlet {
  
  private final Users users;
  private final Templates templates;
  private final EssayistConfig config;
  private final Essays essays;
  private Provider<EssayistSession> sessions;

  @Inject
  public LoginServlet(Users users, Essays essays, Provider<EssayistSession> sessions,
                      Templates jamonContext, EssayistConfig config) {
    this.users = users;
    this.essays = essays;
    this.sessions = sessions;
    this.templates = jamonContext;
    this.config = config;
  }
  
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException ,IOException {
    if (req.getSession(false) != null) {
      User user = (User) req.getSession().getAttribute(User.class.getName());
      EssayistSession session = sessions.get();
      
      if (!session.isLoggedIn()) {
        AuthResult authResult = (AuthResult) req.getSession().getAttribute(req.getParameter("state"));
        if (authResult != null) {
          user = users.getByEntityOrNull(authResult.profile.getCore().getEntity());
          session.setUser(user);
          req.getSession().removeAttribute("state");
        } else if ("server_error".equals(req.getParameter("error"))) {
          String entity = (String) req.getSession().getAttribute("entity");
          users.delete(entity);
          
          TentClient tentClient = new TentClient(entity);
          RegistrationResponse registrationResponse = register(tentClient, req);
          String redirectUri = registrationResponse.getRedirectUris()[0];
          String authorizationUrl = authorize(tentClient, registrationResponse, redirectUri, req);
          
          resp.sendRedirect(authorizationUrl);

          return;
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
      registrationResponse = register(tentClient, req);
      redirectUri = registrationResponse.getRedirectUris()[0];
    }

    String authorizationUrl = authorize(tentClient, registrationResponse, redirectUri, req);
    
    resp.sendRedirect(authorizationUrl);
  }
  
  private RegistrationResponse register(TentClient tentClient, HttpServletRequest req) throws MalformedURLException {
    tentClient.getProfile();
    
    Map<String, String> scopes = new HashMap<String, String>();
    scopes.put("write_posts", "Allows you to write Essays and re-post, bookmark or favorite other people's Essays.");
    scopes.put("read_posts", "Read Essays and your reactions to Essays.");
    
    URL url = new URL(req.getRequestURL().toString());
    String baseUrl = url.getProtocol() + "://" + url.getAuthority() + req.getContextPath();
    String afterAuthorizationUrl = baseUrl + "/accessToken";
    String afterLoginUrl = baseUrl;
    
    RegistrationRequest registrationRequest = new RegistrationRequest(config.getTitle(),
            "A blogging app for when you need more than 256 characters.", baseUrl,
            new String [] { afterAuthorizationUrl, afterLoginUrl }, scopes);
    
    return tentClient.register(registrationRequest);
  }
  
  private String authorize(TentClient tentClient, RegistrationResponse registrationResponse, String redirectUri, HttpServletRequest req) {
    AuthorizationRequest authorizationRequest = new AuthorizationRequest(registrationResponse.getMacKeyId(), redirectUri);
    authorizationRequest.setScope("write_posts", "read_posts");
    authorizationRequest.setTentPostTypes(Post.Types.essay("v0.1.0"), Post.Types.status("v0.1.0"), Post.Types.photo("v0.1.0"), Post.Types.repost("v0.1.0"), EssayistMetadataContent.URI, Bookmark.URI, Favorite.URI);
    authorizationRequest.setState(UUID.randomUUID().toString());
    String authorizationUrl = tentClient.buildAuthorizationUrl(authorizationRequest);
    
    AuthResult authResult = new AuthResult();
    authResult.profile = tentClient.getProfile();
    authResult.registrationResponse = registrationResponse;
    req.getSession().setAttribute(authorizationRequest.getState(), authResult);
    req.getSession().setAttribute("entity", tentClient.getProfile().getCore().getEntity());
    
    return authorizationUrl;
  }
}
