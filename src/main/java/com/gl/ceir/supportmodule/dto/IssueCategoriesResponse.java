package com.gl.ceir.supportmodule.dto;

import lombok.Data;

import java.util.List;

@Data
public class IssueCategoriesResponse {
    private List<IssueCategory> issue_categories;
    private int total_count;

    @Data
    public static class IssueCategory {
        private int id;
        private Project project;
        private String name;
    }

    @Data
    public static class Project {
        private int id;
        private String name;
    }
}

