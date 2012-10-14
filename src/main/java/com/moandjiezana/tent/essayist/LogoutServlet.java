package com.moandjiezana.tent.essayist;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Singleton
public class LogoutServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    HttpSession session = req.getSession(false);
    
    if (session != null) {
      session.invalidate();
    }
    
    resp.sendRedirect(req.getContextPath());
  }
}
