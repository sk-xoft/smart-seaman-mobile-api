package com.seaman.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseStartupHealthCheckInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseStartupHealthCheckInitializer.class);

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BRIGHT_GREEN = "\u001B[1;92m";
    private static final String ANSI_BRIGHT_RED = "\u001B[1;91m";
    private static final int VALIDATION_TIMEOUT_SECONDS = 5;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Environment environment = applicationContext.getEnvironment();
        String driver = requiredProperty(environment, "smart.seaman.datasource.driver", false);
        String url = requiredProperty(environment, "smart.seaman.datasource.url", false);
        String username = requiredProperty(environment, "smart.seaman.datasource.username", false);
        String password = requiredProperty(environment, "smart.seaman.datasource.password", true);

        try {
            Class.forName(driver);
            validateConnection(url, username, password);
            String onlineMessage = "DATABASE ONLINE - Smart Seaman Mobile API is ready to start.";
            System.out.println(ANSI_BRIGHT_GREEN + onlineMessage + ANSI_RESET);
            logger.info(onlineMessage);
        } catch (Exception ex) {
            String offlineMessage = "DATABASE OFFLINE - Smart Seaman Mobile API startup has been stopped.";
            System.err.println(ANSI_BRIGHT_RED + offlineMessage + ANSI_RESET);
            logger.error(offlineMessage, ex);
            throw new IllegalStateException("Database startup health check failed. API startup has been stopped.", ex);
        }
    }

    protected void validateConnection(String url, String username, String password) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            if (!connection.isValid(VALIDATION_TIMEOUT_SECONDS)) {
                throw new SQLException("Database connection is not valid.");
            }
        }
    }

    private String requiredProperty(Environment environment, String key, boolean allowBlank) {
        String value = environment.getProperty(key);
        if (value == null || (!allowBlank && value.trim().isEmpty()) || isUnresolvedPlaceholder(value)) {
            throw new IllegalStateException("Missing required database property: " + key);
        }
        return value;
    }

    private boolean isUnresolvedPlaceholder(String value) {
        return value.startsWith("${") && value.endsWith("}");
    }
}
