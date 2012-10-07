package com.moandjiezana.tent.essayist;

import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.posts.content.EssayContent;
import com.moandjiezana.tent.client.users.Permissions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pegdown.PegDownProcessor;

@Singleton
public class EssaysServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    TentClient tentClient = tentClient(req);
    
    List<Post> posts = tentClient.getPosts();
    List<Post> essays = new ArrayList<>();
    
    for (Post post : posts) {
      if ("https://tent.io/types/post/essay/v0.1.0".equals(post.getType())) {
        essays.add(post);
      }
    }
    
    new EssayTemplate().render(resp.getWriter(), essays);
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    TentClient tentClient = tentClient(req);
    
    Post post = new Post();
    post.setPublishedAt(System.currentTimeMillis() / 1000);
    Permissions permissions = new Permissions();
    permissions.setPublicVisible(true);
    post.setPermissions(permissions);
    post.setLicenses(new String[] { "http://creativecommons.org/licenses/by/3.0/" });
    EssayContent essay = new EssayContent();
    essay.setTitle(req.getParameter("title"));
    essay.setBody(new PegDownProcessor().markdownToHtml(req.getParameter("body")));
    essay.setExcerpt(req.getParameter("excerpt"));
    post.setContent(essay);
    
    tentClient.write(post);
  }

  private TentClient tentClient(HttpServletRequest req) {
    User user = (User) req.getSession().getAttribute(User.class.getName());
    
    TentClient tentClient = new TentClient(user.getProfile(), Collections.<String>emptyList());
    tentClient.getAsync().setAccessToken(user.getAccessToken());
    
    return tentClient;
  }
}
