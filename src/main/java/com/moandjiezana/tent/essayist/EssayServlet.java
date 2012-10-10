package com.moandjiezana.tent.essayist;

import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.essayist.tent.Entities;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class EssayServlet extends HttpServlet {
  
  private Templates templates;

  @Inject
  public EssayServlet(Templates templates) {
    this.templates = templates;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String path = req.getPathInfo();
    int lastSlash = path.lastIndexOf('/');
    String entityRaw = path.substring(0, lastSlash);
    String essayId = path.substring(lastSlash + 1);
    
    TentClient tentClient = new TentClient(Entities.expandFromUrl(entityRaw));
    tentClient.discover();
    Profile profile = tentClient.getProfile();
    Post post = tentClient.getPost(essayId);
    
    templates.essay().render(resp.getWriter(), post, profile);
  }
}
