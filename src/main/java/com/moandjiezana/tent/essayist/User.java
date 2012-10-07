package com.moandjiezana.tent.essayist;

import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.oauth.AccessToken;

public class User {
  private Profile profile;
  private AccessToken accessToken;
  
  public User(Profile profile, AccessToken accessToken) {
    this.profile = profile;
    this.accessToken = accessToken;
  }

  public Profile getProfile() {
    return profile;
  }

  public AccessToken getAccessToken() {
    return accessToken;
  }
}