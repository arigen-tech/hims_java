package com.hims.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@Slf4j
public class JasperConnectionUtil {

    private final DataSource dataSource;

    public JasperConnectionUtil(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(ConnectionCallback<T> callback) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            log.debug("Database connection acquired successfully");
            return callback.execute(connection);
        } catch (SQLException e) {
            log.error("Database connection error: {}", e.getMessage(), e);
            throw new Exception("Failed to get database connection", e);
        } catch (Exception e) {
            log.error("Error executing database operation: {}", e.getMessage(), e);
            throw e;
        }
        // Connection is automatically closed by try-with-resources
    }

    @FunctionalInterface
    public interface ConnectionCallback<T> {
        T execute(Connection connection) throws Exception;
    }
}