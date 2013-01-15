package com.moandjiezana.tent.essayist.db;

import com.eroi.migrate.Configure;
import com.eroi.migrate.Engine;
import com.moandjiezana.tent.client.internal.com.google.common.base.Throwables;
import com.moandjiezana.tent.essayist.db.migrations.Migration_1;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * User: pjesi
 * Date: 1/15/13
 * Time: 11:17 PM
 */
public class DbSetup {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbSetup.class);

    private DataSource dataSource;
    private QueryRunner queryRunner;
    private Properties properties;

    public void init(){
        Properties defaultProperties = new Properties();
        try {
            defaultProperties.load(getClass().getResourceAsStream("/essayist-defaults.properties"));
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }


        properties = new Properties(defaultProperties);
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

        queryRunner = new QueryRunner(dataSource);
    }

    public void destroy(){
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public QueryRunner getQueryRunner() {
        return queryRunner;
    }

    public Properties getProperties() {
        return properties;
    }
}
