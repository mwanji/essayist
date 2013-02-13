package com.moandjiezana.essayist.sessions;

import co.mewf.merf.Response;
import co.mewf.merf.http.GET;
import co.mewf.merf.http.POST;
import co.mewf.merf.http.Responses;
import co.mewf.merf.http.Url;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.moandjiezana.essayist.auth.Authenticated;
import com.moandjiezana.essayist.config.EssayistConfig;
import com.moandjiezana.essayist.merf.JamonResponse;
import com.moandjiezana.essayist.users.User;
import com.moandjiezana.essayist.users.Users;
import com.moandjiezana.essayist.views.Routes;
import com.moandjiezana.essayist.views.Templates;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@Authenticated
@Url("/settings")
public class SettingsController {

  private Templates templates;
  private EssayistSession essayistSession;
  private Routes routes;
  private EssayistConfig config;
  private Users users;
  private HttpServletRequest req;

  @Inject
  public SettingsController(Templates templates, EssayistSession essayistSession, Routes routes, EssayistConfig config, Users users, HttpServletRequest req) {
    this.templates = templates;
    this.essayistSession = essayistSession;
    this.routes = routes;
    this.config = config;
    this.users = users;
    this.req = req;
  }

  @GET @Url
  public Response viewSettings() {
    User user = essayistSession.getUser();

    return new JamonResponse(templates.settings().makeRenderer(user));
  }

  @POST @Url
  public Response saveSettings() {
    User user = essayistSession.getUser();

    String newDomain = Strings.nullToEmpty(req.getParameter("domain"));
    String currentDomain = Strings.nullToEmpty(user.getDomain());
    String queryString = "";

    if (!newDomain.equals(currentDomain) && !newDomain.isEmpty()) {
      Optional<User> domainOwner = users.getUserByDomain(newDomain);

      if (domainOwner.isPresent()) {
        return Responses.redirect("/settings?error=domain_taken" + queryString);
      }
    }

    user.setDomain(newDomain);
    users.save(user);

    return Responses.redirect("/settings" + queryString);
  }
}
