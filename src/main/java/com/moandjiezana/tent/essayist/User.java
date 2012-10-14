package com.moandjiezana.tent.essayist;

import com.moandjiezana.tent.client.apps.RegistrationResponse;
import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.oauth.AccessToken;

public class User {
  private Profile profile;
  private RegistrationResponse registration;
  private AccessToken accessToken;
  private Long id;
  
  public User(Profile profile, AccessToken accessToken) {
    this(null, profile, null, accessToken);
  }

  public User(Profile profile, RegistrationResponse registration, AccessToken accessToken) {
    this(null, profile, registration, accessToken);
  }

  public User(Long id, Profile profile, RegistrationResponse registration, AccessToken accessToken) {
    this.id = id;
    this.profile = profile;
    this.registration = registration;
    this.accessToken = accessToken;
  }

  public Long getId() {
    return id;
  }

  public Profile getProfile() {
    return profile;
  }

  public AccessToken getAccessToken() {
    return accessToken;
  }

  public RegistrationResponse getRegistration() {
    return registration;
  }
}