package com.moandjiezana.tent.essayist;

import com.google.common.io.CharStreams;
import com.moandjiezana.tent.essayist.auth.Authenticated;
import com.moandjiezana.tent.essayist.text.TextTransformation;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
@Authenticated
public class PreviewServlet extends HttpServlet {
  
  private final TextTransformation textTransformation;

  @Inject
  public PreviewServlet(TextTransformation textTransformation) {
    this.textTransformation = textTransformation;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String body = CharStreams.toString(req.getReader());
    String essay = textTransformation.transformEssay(body);
    
    resp.getWriter().write(essay);
  }
}
