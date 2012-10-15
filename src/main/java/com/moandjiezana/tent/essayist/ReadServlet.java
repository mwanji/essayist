package com.moandjiezana.tent.essayist;

import com.moandjiezana.tent.essayist.tent.Entities;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class ReadServlet extends HttpServlet {
  
  private final Templates templates;

  @Inject
  public ReadServlet(Templates templates) {
    this.templates = templates;
  }
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    templates.read().render(resp.getWriter());
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.sendRedirect(req.getContextPath() + "/" + Entities.getForUrl(req.getParameter("entity")) + "/essays");
  }
}
