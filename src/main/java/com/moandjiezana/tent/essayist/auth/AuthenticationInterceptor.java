package com.moandjiezana.tent.essayist.auth;

import co.mewf.merf.http.Responses;

import com.google.inject.matcher.Matcher;
import com.moandjiezana.essayist.sessions.EssayistSession;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.inject.Inject;
import javax.inject.Provider;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class AuthenticationInterceptor implements MethodInterceptor {

  @Inject
  private Provider<EssayistSession> sessions;

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    if (!sessions.get().isLoggedIn()) {
      return Responses.redirect("/login");
    }

    return invocation.proceed();
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
