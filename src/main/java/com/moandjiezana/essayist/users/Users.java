package com.moandjiezana.essayist.users;

import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.moandjiezana.essayist.utils.Tasks;
import com.moandjiezana.tent.client.TentClient;
import com.moandjiezana.tent.client.apps.RegistrationResponse;
import com.moandjiezana.tent.client.users.Profile;
import com.moandjiezana.tent.oauth.AccessToken;

import java.io.BufferedReader;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Users {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(Users.class);
  
  private final QueryRunner queryRunner;
  private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
  private Tasks tasks;
  
  @Inject
  public Users(Tasks tasks, QueryRunner queryRunner) {
    this.tasks = tasks;
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
  
  public List<User> getAll() {
    try {
      return queryRunner.query("select * from AUTHORIZATIONS", new BeanListHandler<User>(User.class, new BasicRowProcessor(new BeanProcessor() {
        @Override
        protected Object processColumn(ResultSet rs, int index, Class<?> propType) throws SQLException {
          if (Long.class.equals(propType)) {
            return super.processColumn(rs, index, propType);
          }
          
          return gson.fromJson(rs.getString(index), propType);
        }
      })));
    } catch (SQLException e) {
      throw Throwables.propagate(e);
    }
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

  public void delete(String entity) {
    try {
      queryRunner.update("delete from AUTHORIZATIONS where ENTITY=?", entity);
    } catch (SQLException e) {
      throw Throwables.propagate(e);
    }
  }
  
  public void fetch(final String entity) {
    tasks.run(new Runnable() {
      @Override
      public void run() {
        try {
          Profile updatedProfile = new TentClient(entity).getProfile();
          save(new User(updatedProfile));
        } catch (Exception e) {
          LOGGER.error("Could not fetch Profile", Throwables.getRootCause(e));
        }
      }
    });
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
}
