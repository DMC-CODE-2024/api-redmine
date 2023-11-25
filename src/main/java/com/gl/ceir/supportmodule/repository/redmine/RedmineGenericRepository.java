package com.gl.ceir.supportmodule.repository.redmine;

import com.gl.ceir.supportmodule.model.redmine.IssueStatusCounts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("redmineRepository")
public class RedmineGenericRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public RedmineGenericRepository(@Qualifier("redmineJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public IssueStatusCounts getIssueStatusCounts(String authorId, int openStatusId, int inProgressStatusId, int closedStatusId, int resolvedStatusId) {
        String sql = "SELECT " +
                "COUNT(*) AS totalTickets, " +
                "SUM(CASE WHEN status_id = ? THEN 1 ELSE 0 END) AS openTickets, " +
                "SUM(CASE WHEN status_id = ? THEN 1 ELSE 0 END) AS inProgressTickets, " +
                "SUM(CASE WHEN status_id = ? THEN 1 ELSE 0 END) AS closedTickets, " +
                "SUM(CASE WHEN status_id = ? THEN 1 ELSE 0 END) AS resolvedTickets " +
                "FROM issues " +
                "WHERE author_id = ?";

        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(IssueStatusCounts.class),
                openStatusId, inProgressStatusId, closedStatusId, resolvedStatusId, authorId);
    }

    public IssueStatusCounts getIssueStatusCountsForProject(int projectId, int openStatusId, int inProgressStatusId, int closedStatusId, int resolvedStatusId) {
        String sql = "SELECT " +
                "COUNT(*) AS totalTickets, " +
                "SUM(CASE WHEN status_id = ? THEN 1 ELSE 0 END) AS openTickets, " +
                "SUM(CASE WHEN status_id = ? THEN 1 ELSE 0 END) AS inProgressTickets, " +
                "SUM(CASE WHEN status_id = ? THEN 1 ELSE 0 END) AS closedTickets, " +
                "SUM(CASE WHEN status_id = ? THEN 1 ELSE 0 END) AS resolvedTickets " +
                "FROM issues " +
                "WHERE project_id = ?";

        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(IssueStatusCounts.class),
                openStatusId, inProgressStatusId, closedStatusId, resolvedStatusId, projectId);
    }

    public Optional<String> getApiKey(String username) {
        String sql = "SELECT t.value FROM users u LEFT JOIN tokens t ON t.user_id = u.id WHERE u.login = ? AND t.action = 'api'";

        try {
            // Using Object array as arguments in case there are additional parameters
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, new Object[]{username}, String.class));
        } catch (EmptyResultDataAccessException e) {
            // No result found, return Optional.empty()
            return Optional.empty();
        }
    }

    public Optional<String> getAuthorId(String login) {
        String sql = "SELECT u.id from users u where u.login = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, new Object[]{login}, String.class));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }
}
