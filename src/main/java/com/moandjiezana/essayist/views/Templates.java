package com.moandjiezana.essayist.views;

import com.moandjiezana.essayist.config.JamonContext;
import com.moandjiezana.tent.essayist.EssayPage;
import com.moandjiezana.tent.essayist.EssaysPage;
import com.moandjiezana.tent.essayist.LoginTemplate;
import com.moandjiezana.tent.essayist.NewEssayTemplate;
import com.moandjiezana.tent.essayist.NonEditableEssayPage;
import com.moandjiezana.tent.essayist.ReadPage;
import com.moandjiezana.tent.essayist.partials.ReactionList;

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
  
  public EssaysPage essays(String active) {
    return new EssaysPage().setActive(active).setJamonContext(jamonContext.get());
  }
  
  public EssayPage essay() {
    return new EssayPage().setJamonContext(jamonContext.get());
  }

  public NewEssayTemplate newEssay() {
    return new NewEssayTemplate().setJamonContext(jamonContext.get());
  }

  public ReadPage read() {
    return new ReadPage().setJamonContext(jamonContext.get());
  }
  
  public ReactionList reactions() {
    return new ReactionList().setJamonContext(jamonContext.get());
  }

  public NonEditableEssayPage nonEditableEssay() {
    return new NonEditableEssayPage().setJamonContext(jamonContext.get());
  }
}
