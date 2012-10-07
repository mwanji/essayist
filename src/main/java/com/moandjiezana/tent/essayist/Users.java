package com.moandjiezana.tent.essayist;

import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

@Singleton
public class Users {
  
  private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
  
  public User getByEntityOrNull(String entity) {
    return users.get(entity);
  }
  
  public void save(User user) {
    users.put(user.getProfile().getCore().getEntity(), user);
  }
}
