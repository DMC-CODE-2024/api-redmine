package com.gl.ceir.supportmodule.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {"com.gl.ceir.supportmodule.repository.redmine"},
        entityManagerFactoryRef = "redmineEntityManagerFactory",
        transactionManagerRef = "redmineTransactionManager")
@EntityScan("com.gl.ceir.supportmodule.model.redmine")

public class RedmineDbConfig {
    @Bean
    public CommandLineRunner auditDbConnectionCheck(DbConnectionChecker dbConnectionChecker) {
        return args -> dbConnectionChecker.checkAuditDbConnection(redmineDataSource());
    }

    @Bean(name = "redmineEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean redmineEntityManagerFactory(
            @Qualifier("redmineDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(dataSource)
                .packages("com.gl.ceir.supportmodule.model.redmine")
                .persistenceUnit("redmine")
                .properties(jpaProperties())
                .build();
    }

    @Bean(name = "redmineDataSource")
    @ConfigurationProperties(prefix = "redmine.datasource")
    public DataSource redmineDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "redmineTransactionManager")
    public PlatformTransactionManager redmineTransactionManager(
            @Qualifier("redmineEntityManagerFactory") LocalContainerEntityManagerFactoryBean redmineEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(redmineEntityManagerFactory.getObject()));
    }


    protected Map<String, Object> jpaProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.physical_naming_strategy", SpringPhysicalNamingStrategy.class.getName());
        props.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());
        return props;
    }

    @Bean(name = "redmineJdbcTemplate")
    public JdbcTemplate redmineJdbcTemplate(@Qualifier("redmineDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
