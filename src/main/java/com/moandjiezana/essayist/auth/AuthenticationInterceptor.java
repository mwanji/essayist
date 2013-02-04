package com.moandjiezana.essayist.auth;

import co.mewf.merf.http.Responses;

import com.moandjiezana.essayist.sessions.EssayistSession;

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
}
