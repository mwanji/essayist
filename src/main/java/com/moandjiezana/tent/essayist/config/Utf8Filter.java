package com.moandjiezana.tent.essayist.config;

import com.google.common.base.Charsets;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

@Singleton
public class Utf8Filter implements Filter {
  
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    req.setCharacterEncoding(Charsets.UTF_8.toString());
    if (!"application/json".equals(req.getHeader("Accept")) && !req.getRequestURI().startsWith("/assets")) {
      response.setContentType("text/html;charset=" + Charsets.UTF_8);
    }
    
    chain.doFilter(request, response);
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public void destroy() {}

}
