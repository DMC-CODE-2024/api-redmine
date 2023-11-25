package com.gl.ceir.supportmodule.repository.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("appRepository")
public class GenericRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenericRepository(@Qualifier("appJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<String> getEmailFromUsername(String username) {
        String sql = "select up.EMAIL from user_profile up left join users u on u.id = up.USERID  where u.USERNAME = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, new Object[]{username}, String.class));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }
}