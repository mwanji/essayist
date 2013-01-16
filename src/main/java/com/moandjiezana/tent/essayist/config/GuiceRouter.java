package com.moandjiezana.tent.essayist.config;

import co.mewf.merf.routes.ControllerRoute;
import co.mewf.merf.routes.DefaultRouter;

import com.google.inject.Injector;

public class GuiceRouter extends DefaultRouter {

  public GuiceRouter add(Injector injector, Class<?>... classes) {
    for (Class<?> controllerClass : classes) {
      add(new ControllerRoute(controllerClass, injector.getProvider(controllerClass)));
    }
    
    return this;
  }
}
