package com.gl.ceir.supportmodule.repository.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Optional;

@Repository("appRepository")
public class GenericRepository {

    private final JdbcTemplate jdbcTemplate;
    private final Environment environment;

    @Autowired
    public GenericRepository(@Qualifier("appJdbcTemplate") JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
    }

    public Optional<String> getEmailFromUsername(String username) {
//        String sql = "select up.email from user_profile up left join user u on u.id = up.user_id  where u.USERNAME = ?";
        String sql;

        // Check if the active profile is 'mysql'
        if (Arrays.asList(environment.getActiveProfiles()).contains("mysql")) {
            sql = "select up.email from user_profile up left join user u on u.id = up.user_id where u.USERNAME = ?";
        } else {
            // Assume Oracle or other profile
            sql = "select up.email from user_profile up left join users u on u.id = up.user_id where u.USERNAME = ?";
        }
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, new Object[]{username}, String.class));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }
}