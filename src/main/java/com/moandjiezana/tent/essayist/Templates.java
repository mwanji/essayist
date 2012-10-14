package com.moandjiezana.tent.essayist;

import com.moandjiezana.tent.essayist.config.JamonContext;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class Templates {

  private final Provider<JamonContext> jamonContext;

  @Inject
  public Templates(Provider<JamonContext> jamonContext) {
    this.jamonContext = jamonContext;
  }
  
  public LoginTemplate login() {
    return new LoginTemplate().setJamonContext(jamonContext.get());
  }
  
  public EssaysTemplate essays() {
    return new EssaysTemplate().setJamonContext(jamonContext.get());
  }
  
  public EssayTemplate essay() {
    return new EssayTemplate().setJamonContext(jamonContext.get());
  }

  public NewEssayTemplate newEssay() {
    return new NewEssayTemplate().setJamonContext(jamonContext.get());
  }
}
