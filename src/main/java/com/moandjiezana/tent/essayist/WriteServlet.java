package com.moandjiezana.tent.essayist;

import com.moandjiezana.essayist.posts.EssayistMetadataContent;
import com.moandjiezana.essayist.utils.Tasks;
import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Mention;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.posts.PostQuery;
import com.moandjiezana.tent.client.posts.content.EssayContent;
import com.moandjiezana.tent.client.users.Permissions;
import com.moandjiezana.tent.essayist.auth.Authenticated;
import com.moandjiezana.tent.essayist.config.Routes;
import com.moandjiezana.tent.essayist.tent.Entities;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pegdown.PegDownProcessor;

@Singleton
@Authenticated
public class WriteServlet extends HttpServlet {
  
  private Templates templates;
  private Provider<EssayistSession> sessions;
  private Tasks tasks;
  private Provider<Routes> routes;

  @Inject
  public WriteServlet(Provider<EssayistSession> sessions, Templates templates, Provider<Routes> routes, Tasks tasks) {
    this.sessions = sessions;
    this.templates = templates;
    this.routes = routes;
    this.tasks = tasks;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (req.getRequestURI().endsWith("/write")) {
      templates.newEssay().render(resp.getWriter());
      return;
    }
    
    String essayId = req.getPathInfo().substring(1);
    TentClient tentClient = newTentClient();
    List<Post> metadataPosts = tentClient.getPosts(new PostQuery().mentionedPost(essayId).postTypes(EssayistMetadataContent.URI));
    if (metadataPosts.isEmpty()) {
      templates.nonEditableEssay().render(resp.getWriter());
      return;
    }
    
    Post essay = tentClient.getPost(essayId);
    Post metadata = metadataPosts.get(0);
    
    templates.newEssay().setMetadata(metadata).setEssay(essay).setMetadata(metadata).render(resp.getWriter());
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    final TentClient tentClient = newTentClient();
    
    final Post post = newPost();
    EssayContent essay = new EssayContent();
    essay.setTitle(req.getParameter("title"));
    final String body = req.getParameter("body");
    essay.setBody(new PegDownProcessor().markdownToHtml(body));
    essay.setExcerpt(req.getParameter("excerpt"));
    post.setContent(essay);
    
    final Post newPost = tentClient.write(post);
    final String newPostId = newPost.getId();
    final User user = sessions.get().getUser();

    Post metadataPost = newPost();
    metadataPost.getPermissions().setPublic(false);
    EssayistMetadataContent metadata = new EssayistMetadataContent(body);
    metadataPost.setContent(metadata);
    String essayId = newPostId;
    metadataPost.setMentions(new Mention[] { new Mention(user.getProfile().getCore().getEntity(), essayId) });
    
    tentClient.write(metadataPost);
    
    resp.sendRedirect(req.getContextPath() + "/" + Entities.getForUrl(tentClient.getProfile().getCore().getEntity()) + "/essay/" + newPost.getId());

//    tasks.run(new Runnable() {
//      @Override
//      public void run() {
//        } else {
//          Post originalPost = posts.get(0);
//          
//          Post newPost = newPost();
//          
//          EssayistMetadataContent content = originalPost.getContentAs(EssayistMetadataContent.class);
//          content.setRaw(body);
//        }
//        
//      }
//    });
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    TentClient tentClient = newTentClient();
//    List<Post> posts = tentClient.getPosts(new PostQuery().mentionedPost(essayId).postTypes(EssayistMetadataContent.URI));
    
    String essayId = req.getPathInfo().substring(1);
    Post post = newPost();
    post.setId(essayId);
    post.setEntity(sessions.get().getUser().getProfile().getCore().getEntity());
    EssayContent essay = new EssayContent();
    essay.setTitle(req.getParameter("title"));
    final String body = req.getParameter("body");
    essay.setBody(new PegDownProcessor().markdownToHtml(body));
    essay.setExcerpt(req.getParameter("excerpt"));
    post.setContent(essay);
    
    tentClient.put(post);
    
    List<Post> posts = tentClient.getPosts(new PostQuery().mentionedPost(essayId).postTypes(EssayistMetadataContent.URI));
    
    Post metadataPost = posts.get(0);
    EssayistMetadataContent metadata = metadataPost.getContentAs(EssayistMetadataContent.class);
    metadata.setRaw(body);
    metadataPost.setContent(metadata);
    
    tentClient.put(metadataPost);
    
    resp.sendRedirect(routes.get().essay(post));
  }

  private Post newPost() {
    Post post = new Post();
    post.setPublishedAt(System.currentTimeMillis() / 1000);
    Permissions permissions = new Permissions();
    permissions.setPublic(true);
    post.setPermissions(permissions);
    post.setLicenses(new String[] { "http://creativecommons.org/licenses/by/3.0/" });
    return post;
  }

  private TentClient newTentClient() {
    User user = sessions.get().getUser();
    
    TentClient tentClient = new TentClient(user.getProfile());
    tentClient.getAsync().setAccessToken(user.getAccessToken());
    tentClient.getAsync().setRegistrationResponse(user.getRegistration());
    
    return tentClient;
  }
}
