package com.moandjiezana.tent.essayist.config;

import com.moandjiezana.tent.client.internal.com.google.common.base.Throwables;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.JdbcInterceptor;
import org.apache.tomcat.jdbc.pool.PooledConnection;

public class EssayistJdbcInterceptor extends JdbcInterceptor {
  
  private final AtomicBoolean initialised = new AtomicBoolean();

  @Override
  public void reset(ConnectionPool parent, PooledConnection con) {
    if (initialised.get() || con == null) {
      return;
    }
    
    Statement statement = null;
    try {
      statement = con.getConnection().createStatement();
      statement.execute("CREATE TABLE IF NOT EXISTS version (version int primary key)");
      statement.execute("INSERT INTO version values (0)");
      statement.close();
      initialised.set(true);
    } catch (SQLException e) {
      throw Throwables.propagate(e);
    } finally {
      try {
        if (statement != null) {
          statement.close();
        }
      } catch (SQLException e) {
        throw Throwables.propagate(e);
      }
    }
  }

}
