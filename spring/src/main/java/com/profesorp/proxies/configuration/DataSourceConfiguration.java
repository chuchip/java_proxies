package com.profesorp.proxies.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfiguration {
    @Bean
    @Primary
    public DataSource tenantDataSource() {
        DataSource dataSource= getTenantDataSource();
        return new ProxyDataSource(dataSource);
    }

    @Bean
    @Qualifier("tenantHikariDataSource")
    public HikariDataSource getTenantDataSource()
    {
        HikariDataSource dataSource = tenantDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();

        return dataSource;
    }
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties tenantDataSourceProperties() {
        return new DataSourceProperties();
    }
}
