package com.moandjiezana.tent.essayist;

import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Post;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class UserServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String entity = req.getParameter("entity");
    
    TentClient tentClient = new TentClient(entity);
    tentClient.discover();
    tentClient.getProfile();
    
    List<Post> essays = new ArrayList<>();
    
    for (Post post : tentClient.getPosts()) {
      if ("https://tent.io/types/post/essay/v0.1.0".equals(post.getType())) {
        essays.add(post);
      }
    }
    
    new EssaysTemplate().render(resp.getWriter(), essays);
  }
}
