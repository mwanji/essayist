package com.moandjiezana.essayist.tent.posts;

import com.moandjiezana.tent.client.posts.content.PostContent;

public class Favorite implements PostContent {
  
  public static final String URI = "http://www.beberlei.de/tent/favorite/v0.0.1";
  
  private String entity;
  private String post;

  public Favorite(String entity, String post) {
    this.entity = entity;
    this.post = post;
  }
  
  public Favorite() {}

  @Override
  public String getType() {
    return URI;
  }

  public String getEntity() {
    return entity;
  }

  public void setEntity(String entity) {
    this.entity = entity;
  }

  public String getPost() {
    return post;
  }

  public void setPost(String post) {
    this.post = post;
  }

}
