package com.example.lottery.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static HikariDataSource dataSource;

    public static DataSource getDataSource() {
        if (dataSource == null) {
            Properties props = new Properties();
            try (InputStream is = DatabaseConfig.class.getClassLoader()
                    .getResourceAsStream("application.properties")) {
                props.load(is);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load DB config", e);
            }

            // Разрешаем переопределение через переменные окружения (для Docker)
            String url = System.getenv().getOrDefault("DB_URL", props.getProperty("db.url"));
            String user = System.getenv().getOrDefault("DB_USERNAME", props.getProperty("db.username"));
            String pass = System.getenv().getOrDefault("DB_PASSWORD", props.getProperty("db.password"));

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(pass);
            config.setMaximumPoolSize(Integer.parseInt(
                props.getProperty("db.pool.max.size", "10")));
            config.setAutoCommit(false);
            config.setTransactionIsolation("TRANSACTION_READ_COMMITTED");

            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }
}