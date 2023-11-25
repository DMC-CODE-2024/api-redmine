package com.gl.ceir.supportmodule.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DbConnectionChecker {
    private final Logger log = LogManager.getLogger(getClass());

    public void checkAppDbConnection(@Qualifier("appDataSource") DataSource dataSource) {
        checkDbConnection(dataSource, "app");
    }

    public void checkAuditDbConnection(@Qualifier("redmineDataSource") DataSource dataSource) {
        checkDbConnection(dataSource, "redmine");
    }

    private void checkDbConnection(DataSource dataSource, String dbName) {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("Database connection for " + dbName + " is successful!");
        } catch (SQLException e) {
            log.error("alert1301: DB connection failed for " + dbName + " while getting DB configuration value");
            e.printStackTrace();
        }
    }
}