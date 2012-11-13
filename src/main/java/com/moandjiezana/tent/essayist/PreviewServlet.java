package com.moandjiezana.tent.essayist;

import com.google.common.io.CharStreams;
import com.moandjiezana.tent.essayist.auth.Authenticated;
import com.moandjiezana.tent.essayist.security.Csrf;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pegdown.PegDownProcessor;

@Singleton
@Authenticated
public class PreviewServlet extends HttpServlet {
  
  private final Csrf csrf;

  @Inject
  public PreviewServlet(Csrf csrf) {
    this.csrf = csrf;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String body = CharStreams.toString(req.getReader());
    String html = new PegDownProcessor().markdownToHtml(body);
    String sanitized = csrf.stripScripts(html);
    
    resp.getWriter().write(sanitized);
  }
}
