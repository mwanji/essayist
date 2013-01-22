package com.moandjiezana.essayist.sessions;

import co.mewf.merf.Response;
import co.mewf.merf.http.GET;
import co.mewf.merf.http.POST;
import co.mewf.merf.http.Responses;
import co.mewf.merf.http.Url;

import com.moandjiezana.essayist.auth.AuthResult;
import com.moandjiezana.essayist.merf.JamonResponse;
import com.moandjiezana.essayist.tent.posts.Bookmark;
import com.moandjiezana.essayist.tent.posts.EssayistMetadataContent;
import com.moandjiezana.essayist.tent.posts.Favorite;
import com.moandjiezana.essayist.users.User;
import com.moandjiezana.essayist.users.Users;
import com.moandjiezana.essayist.views.Templates;
import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.apps.App;
import com.moandjiezana.tent.client.apps.AuthorizationRequest;
import com.moandjiezana.tent.client.apps.RegistrationRequest;
import com.moandjiezana.tent.client.apps.RegistrationResponse;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.oauth.AccessToken;

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
  private final EssayistSession session;

  @Inject
  public SessionController(Users users, Templates jamonContext, EssayistSession session, HttpServletRequest req) {
    this.users = users;
    this.templates = jamonContext;
    this.session = session;
    this.req = req;
  }

  @GET @Url("/")
  public Response root(HttpServletRequest req, HttpServletResponse resp) throws MalformedURLException {
    return loginOrRedirect();
  }

  @GET @Url("/login")
  public Response loginOrRedirect() throws MalformedURLException {
    AuthResult authResult = session.consumeAuthResult();
    String errorParameter = req.getParameter("error");

    if (!session.isLoggedIn() && authResult == null && !"server_error".equals(errorParameter)) {
      return new JamonResponse(templates.login().makeRenderer());
    }

    if ("server_error".equals(req.getParameter("error"))) {
      String entity = (String) req.getSession().getAttribute("entity");
      users.delete(entity);

      TentClient tentClient = new TentClient(entity);
      RegistrationResponse registrationResponse = register(tentClient, req);
      String redirectUri = registrationResponse.getRedirectUris()[0];
      String authorizationUrl = authorize(tentClient, registrationResponse, redirectUri, req);

      return Responses.redirect(authorizationUrl);
    }

    User user = session.getUser();

    if (authResult != null) {
      user = users.getByEntityOrNull(authResult.profile.getCore().getEntity());
      session.setUser(user);
    }

    if (!session.isLoggedIn()) {
      return Responses.status(HttpServletResponse.SC_BAD_REQUEST);
    }

    return Responses.redirect("/read");
  }

  @POST @Url("/login")
  public Response postLogin() throws MalformedURLException {
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
      App app = tentClient.getApp();

      if (app != null && app.getAuthorizations().length > 0) {
        redirectUri = user.getRegistration().getRedirectUris()[1];
        registrationResponse = user.getRegistration();
        String authorizationUrl = authorize(tentClient, registrationResponse, redirectUri, req);

        return Responses.redirect(authorizationUrl);
      }
    }

    return register(entity);
  }

  @GET @Url("/accessToken")
  public Response accessToken() {
    AuthResult authResult = session.consumeAuthResult();

    if (authResult == null) {
      return Responses.status(HttpServletResponse.SC_FORBIDDEN, "No corresponding authentication request found.");
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
    session.setUser(user);

    return Responses.redirect("/read");
  }

  @GET @Url("/logout")
  public Response logOut() {
    HttpSession session = req.getSession(false);

    if (session != null) {
      session.invalidate();
    }

    return Responses.redirect("/");
  }

  private Response register(String entity) throws MalformedURLException {
    TentClient tentClient = new TentClient(entity);
    RegistrationResponse registrationResponse = register(tentClient, req);
    String authorizationUrl = authorize(tentClient, registrationResponse, registrationResponse.getRedirectUris()[0], req);

    return Responses.redirect(authorizationUrl);
  }

  private RegistrationResponse register(TentClient tentClient, HttpServletRequest req) throws MalformedURLException {
    tentClient.getProfile();

    Map<String, String> scopes = new HashMap<String, String>();
    scopes.put("write_posts", "Allows you to write Essays and re-post, bookmark or favorite other people's Essays.");
    scopes.put("read_posts", "Read Essays and your reactions to Essays.");

    URL url = new URL(req.getRequestURL().toString());
    String baseUrl = url.getProtocol() + "://" + url.getAuthority() + req.getContextPath() + "/";
    String afterAuthorizationUrl = baseUrl + "accessToken";
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
    if (!authorizationUrl.endsWith("/")) {
      authorizationUrl += "/";
    }

    AuthResult authResult = new AuthResult();
    authResult.profile = tentClient.getProfile();
    authResult.registrationResponse = registrationResponse;
    req.getSession().setAttribute(authorizationRequest.getState(), authResult);
    req.getSession().setAttribute("entity", tentClient.getProfile().getCore().getEntity());

    return authorizationUrl;
  }

}
