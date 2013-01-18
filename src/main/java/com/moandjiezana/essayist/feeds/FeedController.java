package com.moandjiezana.essayist.feeds;

import co.mewf.merf.Response;
import co.mewf.merf.http.GET;
import co.mewf.merf.http.Url;

import com.moandjiezana.essayist.merf.JamonResponse;
import com.moandjiezana.essayist.sessions.EssayistSession;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.essayist.Essays;
import com.moandjiezana.tent.essayist.Templates;
import com.moandjiezana.tent.essayist.User;
import com.moandjiezana.tent.essayist.Users;
import com.moandjiezana.tent.essayist.auth.Authenticated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

public class FeedController {
  private final EssayistSession session;
  private final Essays essays;
  private final Users users;
  private final Templates templates;

  @Inject
  public FeedController(Essays essays, Users users, Templates templates, EssayistSession session) {
    this.essays = essays;
    this.users = users;
    this.templates = templates;
    this.session = session;
  }

  @Authenticated
  @GET @Url("/read")
  public Response read() {
    User user = session.getUser();

    List<Post> essaysFeed = essays.getFeed(user);
    List<User> allUsers = users.getAll();

    final Map<String, Profile> profiles = new ConcurrentHashMap<String, Profile>();

    for (User aUser : allUsers) {
      profiles.put(aUser.getProfile().getCore().getEntity(), aUser.getProfile());
    }

    for (Post essay : essaysFeed) {
      if (!profiles.containsKey(essay.getEntity())) {
        users.fetch(essay.getEntity());
      }
    }

    return new JamonResponse(templates.read().setEssays(essaysFeed).makeRenderer(profiles));
  }

  @GET @Url("/global")
  public Response global() {
    List<User> allUsers = users.getAll();
    List<Post> essays = this.essays.getEssays(allUsers);

    Map<String, Profile> profiles = new HashMap<String, Profile>();
    for (User aUser : allUsers) {
      profiles.put(aUser.getProfile().getCore().getEntity(), aUser.getProfile());
    }

    return new JamonResponse(templates.read().setEssays(essays).makeRenderer(profiles));
  }
}
