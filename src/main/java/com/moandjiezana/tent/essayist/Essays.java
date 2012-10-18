package com.moandjiezana.tent.essayist;

import com.google.common.base.Throwables;
import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.TentClientAsync;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.posts.PostQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import org.slf4j.LoggerFactory;

public class Essays {
  
  public List<Post> getEssays(List<User> users, TentClient tentClient) {
    CopyOnWriteArrayList<Future<List<Post>>> futurePosts = new CopyOnWriteArrayList<Future<List<Post>>>();
    
    for (User user : users) {
      futurePosts.add(new TentClientAsync(user.getProfile()).getPosts(new PostQuery().entity(user.getProfile().getCore().getEntity()).postTypes(Post.Types.essay("v0.1.0"))));
    }
    
    List<Post> posts = new ArrayList<Post>(futurePosts.size());
    for (Future<List<Post>> postFuture : futurePosts) {
      try {
        posts.addAll(postFuture.get());
      } catch (Exception e) {
        LoggerFactory.getLogger(Essays.class).error("Could not load Post", Throwables.getRootCause(e));
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
}
