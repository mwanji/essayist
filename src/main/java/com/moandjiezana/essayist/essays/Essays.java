package com.moandjiezana.essayist.essays;

import com.google.common.base.Throwables;
import com.moandjiezana.essayist.users.User;
import com.moandjiezana.tent.client.HttpTentDataSource;
import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.posts.PostQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Essays {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(Essays.class);

  public List<Post> getEssays(List<User> users) {
    CopyOnWriteArrayList<Future<List<Post>>> futurePosts = new CopyOnWriteArrayList<Future<List<Post>>>();
    
    for (User user : users) {
      futurePosts.add(new HttpTentDataSource(user.getProfile()).getPosts(new PostQuery().entity(user.getProfile().getCore().getEntity()).postTypes(Post.Types.essay("v0.1.0"))));
    }
    
    List<Post> posts = new ArrayList<Post>(futurePosts.size());
    for (Future<List<Post>> futurePost : futurePosts) {
      try {
        List<Post> post = futurePost.get();
        if (post == null) {
          continue;
        }
        Iterator<Post> iterator = post.iterator();
        while (iterator.hasNext()) {
          if (iterator.next() == null) {
            iterator.remove();
          }
        }
        posts.addAll(post);
      } catch (Exception e) {
        LOGGER.error("Could not load Post", Throwables.getRootCause(e));
      }
    }
    
    Collections.sort(posts, new Comparator<Post>() {
      @Override
      public int compare(Post post1, Post post2) {
        return new Date(post2.getPublishedAt()).compareTo(new Date(post1.getPublishedAt()));
      }
    });
    
    return posts;
  }
  
  public List<Post> getFeed(User user) {
    TentClient tentClient = new TentClient(user.getProfile());
    tentClient.getAsync().setAccessToken(user.getAccessToken());
    tentClient.getAsync().setRegistrationResponse(user.getRegistration());
    
    return tentClient.getPosts(new PostQuery().postTypes(Post.Types.essay("v0.1.0")));
  }
  
  public List<Post> getReactions(User user, String postId) {
    return new TentClient(user.getProfile()).getPosts(new PostQuery().mentionedPost(postId));
  }
  
  public Post get(String essayId) {
    return null;
  }
}
