package com.seaman.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseStartupHealthCheckInitializerTest {

    @Test
    void initializePassesWhenDatabaseConnectionIsValid() {
        DatabaseStartupHealthCheckInitializer initializer = new StubInitializer(false);
        GenericApplicationContext context = contextWithDatabaseProperties();

        assertDoesNotThrow(() -> initializer.initialize(context));
    }

    @Test
    void initializeStopsStartupWhenDatabaseConnectionFails() {
        DatabaseStartupHealthCheckInitializer initializer = new StubInitializer(true);
        GenericApplicationContext context = contextWithDatabaseProperties();

        assertThrows(IllegalStateException.class, () -> initializer.initialize(context));
    }

    @Test
    void initializeStopsStartupWhenRequiredPropertyIsMissing() {
        DatabaseStartupHealthCheckInitializer initializer = new StubInitializer(false);
        GenericApplicationContext context = new GenericApplicationContext();
        context.setEnvironment(new MockEnvironment());

        assertThrows(IllegalStateException.class, () -> initializer.initialize(context));
    }

    @Test
    void initializeAllowsBlankDatabasePassword() {
        DatabaseStartupHealthCheckInitializer initializer = new StubInitializer(false);
        GenericApplicationContext context = contextWithDatabaseProperties("");

        assertDoesNotThrow(() -> initializer.initialize(context));
    }

    private GenericApplicationContext contextWithDatabaseProperties() {
        return contextWithDatabaseProperties("password");
    }

    private GenericApplicationContext contextWithDatabaseProperties(String password) {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("smart.seaman.datasource.driver", "java.lang.String")
                .withProperty("smart.seaman.datasource.url", "jdbc:mysql://localhost:3306/smartseaman")
                .withProperty("smart.seaman.datasource.username", "root")
                .withProperty("smart.seaman.datasource.password", password);

        GenericApplicationContext context = new GenericApplicationContext();
        context.setEnvironment(environment);
        return context;
    }

    private static class StubInitializer extends DatabaseStartupHealthCheckInitializer {

        private final boolean failConnection;

        private StubInitializer(boolean failConnection) {
            this.failConnection = failConnection;
        }

        @Override
        protected void validateConnection(String url, String username, String password) throws SQLException {
            if (failConnection) {
                throw new SQLException("database down");
            }
        }
    }
}
