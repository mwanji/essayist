package com.moandjiezana.tent.essayist;

import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.posts.PostQuery;
import com.moandjiezana.tent.client.posts.content.EssayContent;
import com.moandjiezana.tent.client.users.Permissions;
import com.moandjiezana.tent.essayist.tent.Entities;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pegdown.PegDownProcessor;

@Singleton
public class EssaysServlet extends HttpServlet {
  
  private Templates templates;
  private Users users;

  @Inject
  public EssaysServlet(Users users, Templates templates) {
    this.users = users;
    this.templates = templates;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String pathInfo = req.getPathInfo();
    int lastSlashIndex = pathInfo.lastIndexOf('/');
    
    String entity = Entities.expandFromUrl(pathInfo.substring(0, lastSlashIndex));
    
    TentClient tentClient = getTentClientFromSessionOrUrl(req);
    
    String active = req.getSession().getAttribute(User.class.getName()) != null && entity.equals(tentClient.getProfile().getCore().getEntity()) ? "Written" : "Read";
    
    List<Post> essays = tentClient.getPosts(new PostQuery().postTypes(Post.Types.essay("v0.1.0")).entity(entity));
    
    templates.essays(active).render(resp.getWriter(), essays);
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
    essay.setBody(new PegDownProcessor().markdownToHtml(req.getParameter("body")));
    essay.setExcerpt(req.getParameter("excerpt"));
    post.setContent(essay);
    
    tentClient.write(post);
    
    resp.sendRedirect(req.getRequestURL().toString());
  }

  private TentClient getTentClientFromSessionOrUrl(HttpServletRequest req) {
    User user = (User) req.getSession().getAttribute(User.class.getName());
    
    String pathInfo = req.getPathInfo();
    int lastSlashIndex = pathInfo.lastIndexOf('/');
    String entity = Entities.expandFromUrl(pathInfo.substring(0, lastSlashIndex));
    
    if (user == null || !entity.equals(user.getProfile().getCore().getEntity())) {
      TentClient tentClient = new TentClient(entity);
      tentClient.getProfile();
      
      return tentClient;
    }
    
    TentClient tentClient = new TentClient(user.getProfile());
    tentClient.getAsync().setAccessToken(user.getAccessToken());
    
    return tentClient;
  }
}
