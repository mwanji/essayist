package com.moandjiezana.tent.essayist;

import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.posts.PostQuery;
import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.essayist.auth.Authenticated;
import com.moandjiezana.tent.essayist.tent.Entities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
  public static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
  
  private final Templates templates;
  private Essays essays;
  private Users users;
  private Provider<EssayistSession> sessions;

  @Inject
  public MyFeedServlet(Users users, Essays essays, Provider<EssayistSession> sessions, Templates templates) {
    this.users = users;
    this.essays = essays;
    this.sessions = sessions;
    this.templates = templates;
  }
  
  @Override
  @Authenticated
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    User user = sessions.get().getUser();
    
    TentClient tentClient = new TentClient(user.getProfile());
    tentClient.getAsync().setAccessToken(user.getAccessToken());
    tentClient.getAsync().setRegistrationResponse(user.getRegistration());
    List<Post> essays = tentClient.getPosts(new PostQuery().postTypes(Post.Types.essay("v0.1.0")));
    List<User> allUsers = users.getAll();
    
    final Map<String, Profile> profiles = new ConcurrentHashMap<String, Profile>();
    
    for (User aUser : allUsers) {
      profiles.put(aUser.getProfile().getCore().getEntity(), aUser.getProfile());
    }
    
    List<Callable<Profile>> missingUsers = new ArrayList<Callable<Profile>>();
    int missingUsersCount = 0;
    
    for (Post essay : essays) {
      if (!profiles.containsKey(essay.getEntity())) {
        missingUsersCount++;
      }
    }
    
    final CountDownLatch countDownLatch = new CountDownLatch(missingUsersCount);
    
    for (final Post essay : essays) {
      if (profiles.containsKey(essay.getEntity())) {
        continue;
      }
      
      missingUsers.add(new Callable<Profile>() {
        @Override
        public Profile call() throws Exception {
          try {
            TentClient tentClientAsync = new TentClient(essay.getEntity());
            Profile profile = tentClientAsync.getProfile();
            users.save(new User(profile));
            profiles.put(essay.getEntity(), profile);
            
            return profile;
          } finally {
            countDownLatch.countDown();
          }
        }
      });
    }

    if (!missingUsers.isEmpty()) {
      try {
        EXECUTOR.invokeAll(missingUsers);
        countDownLatch.await();
      } catch (Exception e) {
        LOGGER.error("Problem while fetching missing profiles", e);
      }
    }
    
    templates.read().setEssays(essays).render(resp.getWriter(), profiles);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.sendRedirect(req.getContextPath() + "/" + Entities.getForUrl(req.getParameter("entity")) + "/essays");
  }
}
