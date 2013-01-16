package com.moandjiezana.essayist.sessions;

import co.mewf.merf.Response;
import co.mewf.merf.http.GET;
import co.mewf.merf.http.Responses;
import co.mewf.merf.http.Url;

import com.moandjiezana.essayist.merf.JamonResponse;
import com.moandjiezana.essayist.posts.Bookmark;
import com.moandjiezana.essayist.posts.EssayistMetadataContent;
import com.moandjiezana.essayist.posts.Favorite;
import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.apps.AuthorizationRequest;
import com.moandjiezana.tent.client.apps.RegistrationRequest;
import com.moandjiezana.tent.client.apps.RegistrationResponse;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.essayist.Templates;
import com.moandjiezana.tent.essayist.User;
import com.moandjiezana.tent.essayist.Users;
import com.moandjiezana.tent.essayist.auth.AuthResult;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SessionController {

  private final Users users;
  private final Templates templates;
  private final HttpServletRequest req;
  private final HttpServletResponse resp;
  private final EssayistSession session;

  @Inject
  public SessionController(Users users, Templates jamonContext, EssayistSession session, HttpServletRequest req, HttpServletResponse resp) {
    this.users = users;
    this.templates = jamonContext;
    this.session = session;
    this.req = req;
    this.resp = resp;
  }

  @GET @Url("/")
  public Response root(HttpServletRequest req, HttpServletResponse resp) throws MalformedURLException {
    return loginOrRedirect();
  }

  @GET @Url("/login")
  public Response loginOrRedirect() throws MalformedURLException {
    AuthResult authResult = (AuthResult) req.getSession().getAttribute(req.getParameter("state"));
    String errorParameter = req.getParameter("error");

    if (!session.isLoggedIn() && authResult == null && !"server_error".equals(errorParameter)) {
      return new JamonResponse(templates.login().makeRenderer());
    }

    User user = session.getUser();

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

      return Responses.redirect(authorizationUrl);
    }

    if (user == null) {
      return Responses.status(HttpServletResponse.SC_BAD_REQUEST);
    }

    return Responses.redirect("/read");
  }

  @GET @Url("/logout")
  public Response logOut() {
    HttpSession session = req.getSession(false);

    if (session != null) {
      session.invalidate();
    }

    return Responses.redirect(req.getContextPath());
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

    RegistrationRequest registrationRequest = new RegistrationRequest("Essayist", "A blogging app for when you need more than 256 characters.", baseUrl, new String [] { afterAuthorizationUrl, afterLoginUrl }, scopes);

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
