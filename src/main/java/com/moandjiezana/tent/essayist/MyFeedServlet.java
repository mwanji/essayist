package com.moandjiezana.tent.essayist;

import com.moandjiezana.essayist.sessions.EssayistSession;
import com.moandjiezana.essayist.utils.Tasks;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.essayist.auth.Authenticated;
import com.moandjiezana.tent.essayist.tent.Entities;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MyFeedServlet extends HttpServlet {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(MyFeedServlet.class);
  
  private final Templates templates;
  private Essays essays;
  private Users users;
  private Provider<EssayistSession> sessions;
  private Tasks tasks;

  @Inject
  public MyFeedServlet(Users users, Essays essays, Tasks tasks, Provider<EssayistSession> sessions, Templates templates) {
    this.users = users;
    this.essays = essays;
    this.tasks = tasks;
    this.sessions = sessions;
    this.templates = templates;
  }
  
  @Override
  @Authenticated
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    User user = sessions.get().getUser();
    
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
    
    templates.read().setEssays(essaysFeed).render(resp.getWriter(), profiles);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.sendRedirect(req.getContextPath() + "/" + Entities.getForUrl(req.getParameter("entity")) + "/essays");
  }
}
