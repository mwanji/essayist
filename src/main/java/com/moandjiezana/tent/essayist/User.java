package com.moandjiezana.tent.essayist;

import com.moandjiezana.tent.client.apps.RegistrationResponse;
import com.moandjiezana.tent.client.posts.Post;
import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.oauth.AccessToken;

public class User {
  private Profile profile;
  private RegistrationResponse registration;
  private AccessToken accessToken;
  private Long id;
  private String domain;
  
  public User() {}
  
  public User(String entity) {
    this.profile = new Profile();
    this.profile.setCore(new Profile.Core());
    this.profile.getCore().setEntity(entity);
  }
  
  public User(Profile profile) {
    this(null, profile, null, null, null);
  }
  
  public User(Profile profile, AccessToken accessToken) {
    this(null, profile, null, accessToken, null);
  }

  public User(Profile profile, RegistrationResponse registration, AccessToken accessToken) {
    this(null, profile, registration, accessToken, null);
  }

    public User(Long id, Profile profile, RegistrationResponse registration, AccessToken accessToken) {
        this(null, profile, registration, accessToken, null);

    }

  public User(Long id, Profile profile, RegistrationResponse registration, AccessToken accessToken, String domain) {
    this.id = id;
    this.profile = profile;
    this.registration = registration;
    this.accessToken = accessToken;
    this.domain = domain;
  }
  
  public boolean owns(Post post) {
    return profile.getCore().getEntity().equals(post.getEntity());
  }

  public Long getId() {
    return id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }

  public Profile getProfile() {
    return profile;
  }
  
  public void setProfile(Profile profile) {
    this.profile = profile;
  }

  public AccessToken getAccessToken() {
    return accessToken;
  }
  
  public void setAccessToken(AccessToken accessToken) {
    this.accessToken = accessToken;
  }

  public RegistrationResponse getRegistration() {
    return registration;
  }
  
  public void setRegistration(RegistrationResponse registration) {
    this.registration = registration;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }
}