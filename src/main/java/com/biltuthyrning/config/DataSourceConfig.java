package com.biltuthyrning.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        String raw = databaseUrl;
        log.info("DATABASE_URL received (scheme only): {}",
                raw.contains("@") ? raw.substring(0, raw.indexOf("@")) + "@***" : raw);

        if (!raw.startsWith("postgres://") && !raw.startsWith("postgresql://") && !raw.startsWith("jdbc:")) {
            throw new IllegalStateException(
                "DATABASE_URL ser inte ut som en connection string: '" + raw +
                "'. Förväntat format: postgresql://user:pass@host:port/dbname");
        }

        String url = raw;
        if (url.startsWith("postgres://")) {
            url = "jdbc:postgresql://" + url.substring("postgres://".length());
        } else if (url.startsWith("postgresql://")) {
            url = "jdbc:" + url;
        }

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(url);
        cfg.setDriverClassName("org.postgresql.Driver");
        return new HikariDataSource(cfg);
    }
}
