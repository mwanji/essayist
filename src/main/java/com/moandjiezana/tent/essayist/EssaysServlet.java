package com.moandjiezana.tent.essayist;

import com.moandjiezana.essayist.sessions.EssayistSession;
import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.posts.PostQuery;
import com.moandjiezana.tent.client.posts.content.EssayContent;
import com.moandjiezana.tent.client.users.Permissions;
import com.moandjiezana.tent.essayist.tent.Entities;
import com.moandjiezana.tent.essayist.text.TextTransformation;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class EssaysServlet extends HttpServlet {
  
  private Templates templates;
  private Users users;
  private Provider<EssayistSession> sessions;
  private TextTransformation textTransformation;

  @Inject
  public EssaysServlet(Users users, TextTransformation textTransformation, Templates templates, Provider<EssayistSession> sessions) {
    this.users = users;
    this.textTransformation = textTransformation;
    this.templates = templates;
    this.sessions = sessions;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String pathInfo = req.getPathInfo();
    int lastSlashIndex = pathInfo.lastIndexOf('/');
    
    String entity = Entities.expandFromUrl(pathInfo.substring(0, lastSlashIndex));
    
    TentClient tentClient = getTentClientFromSessionOrUrl(req);
    
    String active = sessions.get().isLoggedIn() && entity.equals(sessions.get().getUser().getProfile().getCore().getEntity()) ? "Written" : "My Feed";
    
    List<Post> essays = tentClient.getPosts(new PostQuery().postTypes(Post.Types.essay("v0.1.0")).entity(entity));
    
    templates.essays(active).render(resp.getWriter(), essays, tentClient.getProfile());
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    TentClient tentClient = getTentClientFromSessionOrUrl(req);
    
    Post post = new Post();
    post.setPublishedAt(System.currentTimeMillis() / 1000);
    Permissions permissions = new Permissions();
    permissions.setPublic(true);
    post.setPermissions(permissions);
    post.setLicenses(new String[] { "http://creativecommons.org/licenses/by/3.0/" });
    EssayContent essay = new EssayContent();
    essay.setTitle(req.getParameter("title"));
    essay.setBody(textTransformation.transformEssay(req.getParameter("body")));
    essay.setExcerpt(req.getParameter("excerpt"));
    post.setContent(essay);
    
    tentClient.write(post);
    
    resp.sendRedirect(req.getRequestURL().toString());
  }

  private TentClient getTentClientFromSessionOrUrl(HttpServletRequest req) {
    User user = sessions.get().getUser();
    
    String pathInfo = req.getPathInfo();
    int lastSlashIndex = pathInfo.lastIndexOf('/');
    String entity = Entities.expandFromUrl(pathInfo.substring(0, lastSlashIndex));
    
    if (!sessions.get().isLoggedIn() || !entity.equals(user.getProfile().getCore().getEntity())) {
      TentClient tentClient = new TentClient(entity);
      tentClient.getProfile();
      
      return tentClient;
    }
    
    TentClient tentClient = new TentClient(user.getProfile());
    tentClient.getAsync().setAccessToken(user.getAccessToken());
    
    return tentClient;
  }
}
