package com.moandjiezana.essayist.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Tasks {

  private final ExecutorService executor = Executors.newCachedThreadPool();

  public Future<?> run(Runnable runnable) {
    return executor.submit(runnable);
  }

  
}
