package com.moandjiezana.tent.essayist.config;

import com.google.common.base.Strings;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

@Singleton
public class HttpMethodFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    
    if (req.getMethod().equals("POST") && !Strings.isNullOrEmpty(req.getParameter("_method"))) {
      req = new HttpServletRequestWrapper(req) {
        @Override
        public String getMethod() {
          return getParameter("_method");
        }
      };
    }
    
    chain.doFilter(req, response);
  }

  @Override
  public void destroy() {}

}
