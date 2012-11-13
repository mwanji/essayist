package com.moandjiezana.tent.essayist.config;

import com.google.common.base.Joiner;
import com.google.inject.servlet.RequestScoped;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.essayist.tent.Entities;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@RequestScoped
public class Routes {

  private final HttpServletRequest req;

  @Inject
  public Routes(HttpServletRequest req) {
    this.req = req;
  }
  
  public String asset(String asset) {
    String url = req.getContextPath() + "/assets/" + asset;
    
    if (asset.endsWith(".css")) {
      return "<link rel=\"stylesheet\" href=\"" + url + "\" />";
    } else if (asset.endsWith(".js")) {
      return "<script src=\"" + url + "\"></script>";
    }
    
    throw new IllegalArgumentException("Unknown asset type: " + asset);
  }
  
  public String assets(String name, String asset, String... assets) {
    List<String> tags = new ArrayList<String>(assets.length);
    tags.add(asset(asset));
    for (String asset2 : assets) {
      tags.add(asset(asset2));
    }
    
    return Joiner.on('\n').join(tags);
  }
  
  public String essay(Post essay) {
    return req.getContextPath() + "/" + Entities.getForUrl(essay.getEntity()) + "/essay/" + essay.getId();
  }
  
  public String comment(Post essay) {
    return essay(essay) + "/status";
  }
}
