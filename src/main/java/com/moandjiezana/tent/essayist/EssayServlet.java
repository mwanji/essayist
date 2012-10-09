package com.moandjiezana.tent.essayist;

import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.users.Profile;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class EssayServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String path = req.getPathInfo();
    String[] parts = path.split("/", 2);
    String entityRaw = parts[1];
    String essayId = parts[0];
    
    if (entityRaw.startsWith("s:")) {
      entityRaw = "https://" + entityRaw.substring(2);
    } else {
      entityRaw = "http://" + entityRaw;
    }
    
    TentClient tentClient = new TentClient(entityRaw);
    tentClient.discover();
    Profile profile = tentClient.getProfile();
    Post post = tentClient.getPost(essayId);
    
    
    new EssayTemplate().render(resp.getWriter(), post, profile);
  }
}
