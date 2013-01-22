package com.moandjiezana.essayist.tent.posts;

import com.moandjiezana.tent.client.posts.content.PostContent;

public class EssayistMetadataContent implements PostContent {

  public static final String URI = "http://moandjiezana.com/tent/essayist/types/post/metadata/v0.1.0";
  
  private String format = "markdown";
  private String raw;
  private String statusId;
  
  public EssayistMetadataContent(String raw) {
    this.raw = raw;
  }

  public EssayistMetadataContent() {}
  
  @Override
  public String getType() {
    return URI;
  }
  
  public String getFormat() {
    return format;
  }
  
  public void setFormat(String format) {
    this.format = format;
  }
  
  public String getRaw() {
    return raw;
  }
  
  public void setRaw(String raw) {
    this.raw = raw;
  }

  public String getStatusId() {
    return statusId;
  }

  public void setStatusId(String statusId) {
    this.statusId = statusId;
  }
}
