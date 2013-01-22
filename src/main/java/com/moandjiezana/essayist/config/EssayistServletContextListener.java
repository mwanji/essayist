package com.moandjiezana.essayist.config;

import co.mewf.merf.Router;
import co.mewf.merf.config.MerfServletContextListener;
import co.mewf.merf.guice.GuiceRouter;

import com.eroi.migrate.Configure;
import com.eroi.migrate.Engine;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import com.moandjiezana.essayist.auth.Authenticated;
import com.moandjiezana.essayist.auth.AuthenticationInterceptor;
import com.moandjiezana.essayist.db.migrations.Migration_1;
import com.moandjiezana.essayist.essays.ReactController;
import com.moandjiezana.essayist.essays.ReadController;
import com.moandjiezana.essayist.essays.WriteController;
import com.moandjiezana.essayist.sessions.SessionController;
import com.moandjiezana.tent.client.internal.com.google.common.base.Throwables;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.ServletContextEvent;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EssayistServletContextListener extends MerfServletContextListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(EssayistServletContextListener.class);
  private DataSource dataSource;

  @Override
  protected Router createRouter() {
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
    poolProperties.setTestWhileIdle(true);
    poolProperties.setTestOnBorrow(true);
    poolProperties.setValidationQuery("SELECT 1");

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

    Module module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(QueryRunner.class).toInstance(queryRunner);

        AuthenticationInterceptor authenticationInterceptor = new AuthenticationInterceptor();
        requestInjection(authenticationInterceptor);
        Matcher<AnnotatedElement> authenticationAnnotationMatcher = Matchers.annotatedWith(Authenticated.class);
        bindInterceptor(authenticationAnnotationMatcher, Matchers.any(), authenticationInterceptor);
        bindInterceptor(Matchers.any(), authenticationAnnotationMatcher, authenticationInterceptor);
      }
    };

    return new GuiceRouter(module).add(SessionController.class, WriteController.class, ReadController.class, ReactController.class);
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    super.contextDestroyed(servletContextEvent);
    if (dataSource != null) {
      dataSource.close();
    }
  }

}
