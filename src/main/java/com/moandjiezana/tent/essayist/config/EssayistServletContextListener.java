package com.moandjiezana.tent.essayist.config;

import com.eroi.migrate.Configure;
import com.eroi.migrate.Engine;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.moandjiezana.tent.client.internal.com.google.common.base.Throwables;
import com.moandjiezana.tent.essayist.AccessTokenServlet;
import com.moandjiezana.tent.essayist.EssayServlet;
import com.moandjiezana.tent.essayist.EssaysServlet;
import com.moandjiezana.tent.essayist.GlobalFeedServlet;
import com.moandjiezana.tent.essayist.LoginServlet;
import com.moandjiezana.tent.essayist.LogoutServlet;
import com.moandjiezana.tent.essayist.MyFeedServlet;
import com.moandjiezana.tent.essayist.NewEssayServlet;
import com.moandjiezana.tent.essayist.db.migrations.Migration_1;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.ServletContextEvent;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EssayistServletContextListener extends GuiceServletContextListener {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(EssayistServletContextListener.class);
  private DataSource dataSource;

  @Override
  protected Injector getInjector() {
    
    Properties defaultProperties = new Properties();
    try {
      defaultProperties.load(getClass().getResourceAsStream("/essayist-defaults.properties"));
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
    
    Properties properties = new Properties(defaultProperties);
    try {
      properties.load(getClass().getResourceAsStream("/essayist.properties"));
    } catch (Exception e) {
      LOGGER.warn("Could not find essayist.properties in root folder. Using essayist-defaults.properties");
    }
    
    PoolProperties poolProperties = new PoolProperties();
    poolProperties.setUsername(properties.getProperty("db.username"));
    poolProperties.setPassword(properties.getProperty("db.password"));
    poolProperties.setUrl(properties.getProperty("db.url"));
    poolProperties.setDriverClassName(properties.getProperty("db.driverClassName"));
    poolProperties.setInitialSize(Integer.parseInt(properties.getProperty("db.initialSize")));
    
    dataSource = new DataSource(poolProperties);
    
    Connection connection = null;
    try {
      connection = dataSource.getConnection();
      Configure.configure(connection, Migration_1.class.getPackage().getName());
      Engine.migrate();
    } catch (SQLException e) {
      throw Throwables.propagate(e);
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    
    final QueryRunner queryRunner = new QueryRunner(dataSource);
    
    return Guice.createInjector(new ServletModule() {
      @Override
      protected void configureServlets() {
        serve("/", "/login").with(LoginServlet.class);
        serve("/logout").with(LogoutServlet.class);
        serve("/accessToken").with(AccessTokenServlet.class);
        serve("/read").with(MyFeedServlet.class);
        serve("/global").with(GlobalFeedServlet.class);
        serve("/write").with(NewEssayServlet.class);
        serveRegex("/(.*)/essays").with(EssaysServlet.class);
        serveRegex("/(.*)/essay/(.*)").with(EssayServlet.class);
        filter("/*").through(Utf8Filter.class);
        filter("/*").through(HttpMethodFilter.class);
      }
    }, new AbstractModule() {
      @Override
      protected void configure() {
        bind(QueryRunner.class).toInstance(queryRunner);
      }
    });
  }
  
  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    super.contextDestroyed(servletContextEvent);
    if (dataSource != null) {
      dataSource.close();
    }
  }

}
