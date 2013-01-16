package com.moandjiezana.tent.essayist.auth;

import com.google.inject.matcher.Matcher;
import com.moandjiezana.essayist.sessions.EssayistSession;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class AuthenticationInterceptor implements MethodInterceptor {

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    HttpServletRequest request = (HttpServletRequest) invocation.getArguments()[0];
    HttpSession session = request.getSession(false);
    if (session == null) {
      
      return refuse(invocation);
    }
    
    Boolean sessionKey = (Boolean) request.getSession().getAttribute(EssayistSession.class.getName());
    if (sessionKey == null || Boolean.FALSE.equals(sessionKey)) {
      return refuse(invocation);
    }
    
    return invocation.proceed();
  }
  
  private Void refuse(MethodInvocation invocation) throws IOException {
    HttpServletRequest request = (HttpServletRequest) invocation.getArguments()[0];
    HttpServletResponse response = (HttpServletResponse) invocation.getArguments()[1];
    response.sendRedirect(request.getContextPath());

    return null;
  }
  
  public static class MethodOfAuthenticatedClassMatcher implements Matcher<Method> {

    @Override
    public boolean matches(Method method) {
      return method.getName().startsWith("do") && method.getModifiers() == Modifier.PROTECTED;
    }

    @Override
    public Matcher<Method> and(Matcher<? super Method> other) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Matcher<Method> or(Matcher<? super Method> other) {
      throw new UnsupportedOperationException();
    }

  }
}
