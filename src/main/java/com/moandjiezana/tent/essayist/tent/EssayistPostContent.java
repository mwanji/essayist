package com.moandjiezana.tent.essayist.tent;

import com.moandjiezana.tent.client.posts.content.EssayContent;

import java.util.HashMap;
import java.util.Map;

public class EssayistPostContent extends EssayContent {

  private Map<String, String> essayist = new HashMap<String, String>();
  
  @Override
  public String getType() {
    return super.getType();
  }
  
  public String getEssayistType() {
    return essayist.get("type");
  }
  
  public void setType(String type) {
    essayist.put("type", type);
  }
  
  public String getRaw() {
    return essayist.get("raw");
  }
  
  public void setRaw(String raw) {
    essayist.put("raw", raw);
  }
}
