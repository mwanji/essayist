package com.moandjiezana.essayist.merf;

import co.mewf.merf.Response;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jamon.Renderer;

public class JamonResponse implements Response {
  private final Renderer renderer;
  
  public JamonResponse(Renderer renderer) {
    this.renderer = renderer;
  }

  @Override
  public void write(HttpServletRequest request, HttpServletResponse response) throws Exception {
    renderer.renderTo(response.getWriter());
  }

}
