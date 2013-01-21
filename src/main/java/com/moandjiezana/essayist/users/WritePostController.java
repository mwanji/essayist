package com.moandjiezana.essayist.users;

import co.mewf.merf.Response;
import co.mewf.merf.http.DELETE;
import co.mewf.merf.http.GET;
import co.mewf.merf.http.POST;
import co.mewf.merf.http.PUT;
import co.mewf.merf.http.Responses;
import co.mewf.merf.http.Url;

import com.google.common.base.Throwables;
import com.moandjiezana.essayist.merf.JamonResponse;
import com.moandjiezana.essayist.posts.EssayistMetadataContent;
import com.moandjiezana.essayist.sessions.EssayistSession;
import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Mention;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.posts.PostQuery;
import com.moandjiezana.tent.client.posts.content.EssayContent;
import com.moandjiezana.tent.client.users.Permissions;
import com.moandjiezana.tent.essayist.Templates;
import com.moandjiezana.tent.essayist.User;
import com.moandjiezana.tent.essayist.auth.Authenticated;
import com.moandjiezana.tent.essayist.config.Routes;
import com.moandjiezana.tent.essayist.tent.Entities;
import com.moandjiezana.tent.essayist.text.TextTransformation;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Authenticated
public class WritePostController {

  private final Templates templates;
  private final EssayistSession session;
  private final Routes route;
  private final TextTransformation textTransformation;
  private final HttpServletRequest req;

  @Inject
  public WritePostController(HttpServletRequest req, TextTransformation textTransformation, EssayistSession session, Templates templates, Routes route) {
    this.req = req;
    this.textTransformation = textTransformation;
    this.session = session;
    this.templates = templates;
    this.route = route;
  }

  @GET @Url("/write")
  public Response newEssayPage() throws ServletException, IOException {
    return new JamonResponse(templates.newEssay().makeRenderer());
  }

  @POST @Url("/write")
  public Response createEssay(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    final TentClient tentClient = newTentClient();

    final Post post = newPost();
    EssayContent essay = new EssayContent();
    essay.setTitle(req.getParameter("title"));
    final String body = req.getParameter("body");
    essay.setBody(textTransformation.transformEssay(body));
    essay.setExcerpt(req.getParameter("excerpt"));
    post.setContent(essay);

    final Post newPost = tentClient.write(post);
    final String newPostId = newPost.getId();
    final User user = session.getUser();

    Post metadataPost = newPost();
    metadataPost.getPermissions().setPublic(false);
    EssayistMetadataContent metadata = new EssayistMetadataContent(body);
    metadataPost.setContent(metadata);
    String essayId = newPostId;
    metadataPost.setMentions(new Mention[] { new Mention(user.getProfile().getCore().getEntity(), essayId) });

    tentClient.write(metadataPost);

    return Responses.redirect("/" + Entities.getForUrl(tentClient.getProfile().getCore().getEntity()) + "/essay/" + newPost.getId());
  }

  @GET @Url("/write/{essayId}")
  public Response editEssayPage(String essayId) {
    TentClient tentClient = newTentClient();
    List<Post> metadataPosts = tentClient.getPosts(new PostQuery().mentionedPost(essayId).postTypes(EssayistMetadataContent.URI));
    if (metadataPosts.isEmpty()) {
      return new JamonResponse(templates.nonEditableEssay().makeRenderer());
    }

    Post essay = tentClient.getPost(essayId);
    Post metadata = metadataPosts.get(0);

    return new JamonResponse(templates.newEssay().setMetadata(metadata).setEssay(essay).makeRenderer());
  }

  @PUT @Url("/write/{essayId}")
  public Response updateEssay(String essayId) {
    TentClient tentClient = newTentClient();

    Post post = newPost();
    post.setId(essayId);
    post.setEntity(session.getUser().getProfile().getCore().getEntity());
    EssayContent essay = new EssayContent();
    essay.setTitle(req.getParameter("title"));
    final String body = req.getParameter("body");
    essay.setBody(textTransformation.transformEssay(body));
    essay.setExcerpt(req.getParameter("excerpt"));
    post.setContent(essay);

    tentClient.put(post);

    List<Post> posts = tentClient.getPosts(new PostQuery().mentionedPost(essayId).postTypes(EssayistMetadataContent.URI));

    Post metadataPost = posts.get(0);
    EssayistMetadataContent metadata = metadataPost.getContentAs(EssayistMetadataContent.class);
    metadata.setRaw(body);
    metadataPost.setContent(metadata);

    tentClient.put(metadataPost);

    return Responses.redirect(route.essay(post));
  }

  @DELETE @Url("/write/{essayId}")
  public Response delete(String essayId) {
    User user = session.getUser();

    String entity = user.getProfile().getCore().getEntity();

    TentClient tentClient = new TentClient(user.getProfile());
    tentClient.getAsync().setAccessToken(user.getAccessToken());
    tentClient.getAsync().setRegistrationResponse(user.getRegistration());

    try {
      tentClient.getAsync().deletePost(essayId).get();
    } catch (Exception e) {
      Throwables.propagate(Throwables.getRootCause(e));
    }

    return Responses.redirect("/" + Entities.getForUrl(entity) + "/essays");
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
    User user = session.getUser();

    TentClient tentClient = new TentClient(user.getProfile());
    tentClient.getAsync().setAccessToken(user.getAccessToken());
    tentClient.getAsync().setRegistrationResponse(user.getRegistration());

    return tentClient;
  }
}