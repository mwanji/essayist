package com.moandjiezana.tent.essayist;

import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.moandjiezana.tent.client.apps.RegistrationResponse;
import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.oauth.AccessToken;

import java.io.BufferedReader;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;

@Singleton
public class Users {
  
  private final QueryRunner queryRunner;
  private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
  
  @Inject
  public Users(QueryRunner queryRunner) {
    this.queryRunner = queryRunner;
  }

  public User getByEntityOrNull(String entity) {
    try {
      Map<String, Object> map = queryRunner.query("SELECT * FROM AUTHORIZATIONS WHERE ENTITY=?", new MapHandler(), entity);
      
      if (map == null) {
        return null;
      }
      
      return new User((Long) map.get("id"), convert(map.get("profile"), Profile.class), convert(map.get("registration"), RegistrationResponse.class), convert(map.get("accessToken"), AccessToken.class));
    } catch (SQLException e) {
      throw Throwables.propagate(e);
    }
  }

  private <T> T convert(Object value, Class<T> objectClass) {
    String s;
    if (value instanceof String) {
      s = (String) value;
    } else {
      Clob clob = (Clob) value;
      
      try {
        s = CharStreams.toString(new BufferedReader(clob.getCharacterStream()));
      } catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }
    
    
    return gson.fromJson(s, objectClass);
  }
  
  public void save(User user) {
    try {
      String profileJson = gson.toJson(user.getProfile());
      String registrationJson = gson.toJson(user.getRegistration());
      String accessTokenJson = gson.toJson(user.getAccessToken());
      
      if (user.getId() != null) {
        queryRunner.update("UPDATE AUTHORIZATIONS SET PROFILE=?, REGISTRATION=?, ACCESSTOKEN=? WHERE ID=?", profileJson, registrationJson, accessTokenJson, user.getId());
      } else {
        queryRunner.update("INSERT INTO AUTHORIZATIONS(ENTITY, PROFILE, REGISTRATION, ACCESSTOKEN) VALUES(?,?,?,?)", user.getProfile().getCore().getEntity(), profileJson, registrationJson, accessTokenJson);
      }
    } catch (SQLException e) {
      throw Throwables.propagate(e);
    }
  }
}
