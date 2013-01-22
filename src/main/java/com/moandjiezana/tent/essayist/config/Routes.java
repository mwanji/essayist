package com.moandjiezana.tent.essayist.config;

import com.google.common.base.Joiner;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.essayist.tent.Entities;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.webjars.AssetLocator;

public class Routes {

  private final HttpServletRequest req;

  @Inject
  public Routes(HttpServletRequest req) {
    this.req = req;
  }

  public String webJar(String asset) {
    String url = req.getContextPath() + "/" + AssetLocator.getWebJarPath(asset);

    if (asset.endsWith(".css")) {
      return "<link href=\"" + url + "\"  rel=\"stylesheet\" type=\"text/css\" >";
    } else if (asset.endsWith(".js")) {
      return "<script src=\"" + url + "\" type=\"text/javascript\"></script>";
    }

    throw new IllegalArgumentException("Unknown asset type: " + asset);
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
    return "/" + Entities.getForUrl(essay.getEntity()) + "/essay/" + essay.getId();
  }

  public String essayPath(Post essay) {
    return req.getContextPath() + essay(essay);
  }

  public String essaysPath(String entity) {
    return req.getContextPath() + "/" + Entities.getForUrl(entity) + "/essays";
  }

  public String comment(Post essay) {
    return essayPath(essay) + "/status";
  }
}
