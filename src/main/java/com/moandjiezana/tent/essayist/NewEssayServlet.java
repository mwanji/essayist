package com.moandjiezana.tent.essayist;

import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.users.Permissions;
import com.moandjiezana.tent.essayist.tent.Entities;
import com.moandjiezana.tent.essayist.tent.EssayistPostContent;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pegdown.PegDownProcessor;

@Singleton
public class NewEssayServlet extends HttpServlet {
  
  private Templates templates;

  @Inject
  public NewEssayServlet(Templates templates) {
    this.templates = templates;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    templates.newEssay().render(resp.getWriter());
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    User user = (User) req.getSession().getAttribute(User.class.getName());
    TentClient tentClient = tentClient(req);
    
    Post post = new Post();
    post.setPublishedAt(System.currentTimeMillis() / 1000);
    Permissions permissions = new Permissions();
    permissions.setPublic(true);
    post.setPermissions(permissions);
    post.setLicenses(new String[] { "http://creativecommons.org/licenses/by/3.0/" });
    EssayistPostContent essay = new EssayistPostContent();
    essay.setTitle(req.getParameter("title"));
    essay.setBody(new PegDownProcessor().markdownToHtml(req.getParameter("body")));
    essay.setRaw(req.getParameter("body"));
    essay.setType("markdown");
    essay.setExcerpt(req.getParameter("excerpt"));
    post.setContent(essay);
    
    tentClient.write(post);
    
    resp.sendRedirect(req.getContextPath() + "/" + Entities.getForUrl(user.getProfile().getCore().getEntity()) + "/essays");
  }

  private TentClient tentClient(HttpServletRequest req) {
    User user = (User) req.getSession().getAttribute(User.class.getName());
    
    TentClient tentClient = new TentClient(user.getProfile());
    tentClient.getAsync().setAccessToken(user.getAccessToken());
    
    return tentClient;
  }
}
