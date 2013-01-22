package com.moandjiezana.essayist.tent.posts;

import com.moandjiezana.tent.client.posts.content.PostContent;

import java.net.URL;
import java.util.Locale;

public class Bookmark implements PostContent {
  
  public static final String URI = "http://www.beberlei.de/tent/bookmark/v0.0.1";
  
  private URL url;
  private String title;
  private URL image;
  private String description;
  private Locale[] locale;
  private String siteName;
  private String[] tags;
  private String content;
  
  public Bookmark(URL url, String title) {
    this.url = url;
    this.title = title;
  }

  public Bookmark() {}

  @Override
  public String getType() {
    return URI;
  }

  public URL getUrl() {
    return url;
  }

  public void setUrl(URL url) {
    this.url = url;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public URL getImage() {
    return image;
  }

  public void setImage(URL image) {
    this.image = image;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Locale[] getLocale() {
    return locale;
  }

  public void setLocale(Locale[] locale) {
    this.locale = locale;
  }

  public String getSiteName() {
    return siteName;
  }

  public void setSiteName(String siteName) {
    this.siteName = siteName;
  }

  public String[] getTags() {
    return tags;
  }

  public void setTags(String[] tags) {
    this.tags = tags;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

}
