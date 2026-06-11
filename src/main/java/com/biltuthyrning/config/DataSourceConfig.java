package com.biltuthyrning.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("prod")
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${DATABASE_URL}")
    private String databaseUrl;

    @Bean
    @Primary
    public DataSource dataSource() {
        // Render ger:  postgresql://user:pass@host:port/dbname
        // JDBC kräver: jdbc:postgresql://host:port/dbname  + username/password separat
        String raw = databaseUrl;

        String withoutScheme;
        if (raw.startsWith("postgres://")) {
            withoutScheme = raw.substring("postgres://".length());
        } else if (raw.startsWith("postgresql://")) {
            withoutScheme = raw.substring("postgresql://".length());
        } else {
            throw new IllegalStateException(
                "DATABASE_URL har oväntat format: " + raw.substring(0, Math.min(raw.length(), 30)));
        }

        // Plocka ut user:pass (före @) och host/db (efter @)
        int atIndex = withoutScheme.indexOf('@');
        String username = null;
        String password = null;
        String hostAndDb;

        if (atIndex >= 0) {
            String userPass = withoutScheme.substring(0, atIndex);
            hostAndDb = withoutScheme.substring(atIndex + 1);
            int colon = userPass.indexOf(':');
            if (colon >= 0) {
                username = userPass.substring(0, colon);
                password = userPass.substring(colon + 1);
            } else {
                username = userPass;
            }
        } else {
            hostAndDb = withoutScheme;
        }

        String jdbcUrl = "jdbc:postgresql://" + hostAndDb;
        log.info("Connecting to PostgreSQL: jdbc:postgresql://{}", hostAndDb);

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(jdbcUrl);
        cfg.setDriverClassName("org.postgresql.Driver");
        if (username != null) cfg.setUsername(username);
        if (password != null) cfg.setPassword(password);

        return new HikariDataSource(cfg);
    }
}
