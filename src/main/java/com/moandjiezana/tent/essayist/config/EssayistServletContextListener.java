package com.moandjiezana.tent.essayist.config;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.moandjiezana.tent.essayist.AccessTokenServlet;
import com.moandjiezana.tent.essayist.EssayServlet;
import com.moandjiezana.tent.essayist.EssaysServlet;
import com.moandjiezana.tent.essayist.LoginServlet;
import com.moandjiezana.tent.essayist.UserServlet;

public class EssayistServletContextListener extends GuiceServletContextListener {

  @Override
  protected Injector getInjector() {
    return Guice.createInjector(new ServletModule() {
      @Override
      protected void configureServlets() {
        serve("/", "/login").with(LoginServlet.class);
        serve("/accessToken").with(AccessTokenServlet.class);
        serve("/essays").with(EssaysServlet.class);
        serveRegex("/essay/(.*)/(.*)").with(EssayServlet.class);
        serve("/user", "/user/*").with(UserServlet.class);
      }
    });
  }

}
