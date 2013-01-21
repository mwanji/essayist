package com.moandjiezana.tent.essayist;

import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.posts.PostQuery;
import com.moandjiezana.tent.client.posts.content.EssayContent;
import com.moandjiezana.tent.client.users.Permissions;
import com.moandjiezana.tent.essayist.config.EntityLookup;
import com.moandjiezana.tent.essayist.text.TextTransformation;
import fj.data.Option;

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
  private EntityLookup entityLookup;
  private TextTransformation textTransformation;

  @Inject
  public EssaysServlet(Users users, TextTransformation textTransformation,
                       Templates templates, Provider<EssayistSession> sessions,
                       EntityLookup entityLookup) {
    this.users = users;
    this.textTransformation = textTransformation;
    this.templates = templates;
    this.sessions = sessions;
    this.entityLookup = entityLookup;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    Option<String> optionalEntity = entityLookup.getEntity(req);

    // TODO 404 if not found
    String entity = optionalEntity.some();

    TentClient tentClient = getTentClientFromSessionOrUrl(entity);
    
    String active = sessions.get().isEntity(entity)
            ? "Written" : "My Feed";
    
    List<Post> essays = tentClient.getPosts(
            new PostQuery().postTypes(Post.Types.essay("v0.1.0")).entity(entity));
    
    templates.essays(active).render(resp.getWriter(), essays, tentClient.getProfile());
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
          throws ServletException, IOException {
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
        // TODO deal with missing entity (404)
        Option<String> optionalEntity = entityLookup.getEntity(req);

        return getTentClientFromSessionOrUrl(optionalEntity.some());

    }

  private TentClient getTentClientFromSessionOrUrl(String entity) {
    User user = sessions.get().getUser();

    if (!sessions.get().isEntity(entity)) {
      TentClient tentClient = new TentClient(entity);
      tentClient.getProfile();
      
      return tentClient;
    }
    
    TentClient tentClient = new TentClient(user.getProfile());
    tentClient.getAsync().setAccessToken(user.getAccessToken());
    
    return tentClient;
  }
}
