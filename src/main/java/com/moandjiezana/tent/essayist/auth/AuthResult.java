package com.moandjiezana.tent.essayist.auth;

import com.moandjiezana.tent.client.apps.RegistrationResponse;
import com.moandjiezana.tent.client.users.Profile;

public class AuthResult {
  public Profile profile;
  public RegistrationResponse registrationResponse;
  public String state;
}