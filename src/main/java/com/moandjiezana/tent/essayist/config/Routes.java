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
  private final EssayistConfig config;

    @Inject
  public Routes(HttpServletRequest req, EssayistConfig config) {
    this.req = req;
    this.config = config;
  }
  
  public String asset(String asset) {
    String url = req.getContextPath() + "/assets/" + asset;
    
    if (asset.endsWith(".css")) {
      return "<link href=\"" + url + "\"  rel=\"stylesheet\" type=\"text/css\" >";
    } else if (asset.endsWith(".js")) {
      return "<script src=\"" + url + "\" type=\"text/javascript\"></script>";
    }
    
    throw new IllegalArgumentException("Unknown asset type: " + asset);
  }
  
  public String assets(String name, String asset, String... assets) {
    List<String> tags = new ArrayList<String>(assets.length);
    tags.add(asset(asset));
    for (String asset2 : assets) {
      tags.add(asset(asset2));
    }
    
    return Joiner.on("\n").join(tags);
  }
  
  public String essay(Post essay) {
      return getEssaysUrl(essay);
    //return req.getContextPath() + "/" + Entities.getForUrl(essay.getEntity()) + "/essay/" + essay.getId();
  }

  private String getEssaysUrl(Post essay){
        StringBuilder builder = new StringBuilder(req.getContextPath());

        if(!config.isDefaultEntity(essay.getEntity())){
            builder.append("/");
            builder.append(Entities.getForUrl(essay.getEntity()));
        }

        builder.append("/essay/");
        builder.append(essay.getId());

        return builder.toString();
  }
  
  public String comment(Post essay) {
    return essay(essay) + "/status";
  }
}
