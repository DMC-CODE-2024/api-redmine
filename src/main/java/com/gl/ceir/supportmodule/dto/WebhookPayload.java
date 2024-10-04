package com.gl.ceir.supportmodule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookPayload {
    @JsonProperty("payload")
    private Payload payload;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {
        @JsonProperty("action")
        private String action;
        @JsonProperty("issue")
        private Issue issue;
        @JsonProperty("journal")
        private Journal journal;
        @JsonProperty("url")
        private String url;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Issue {
        private int id;
        private String subject;
        private String description;
        private String created_on;
        private String updated_on;
        private String closed_on;
        private int root_id;
        private Integer parent_id;
        private int done_ratio;
        private String start_date;
        private String due_date;
        private String estimated_hours;
        private boolean is_private;
        private int lock_version;
        private List<CustomField> custom_field_values;
        private Project project;
        private Status status;
        private Tracker tracker;
        private Priority priority;
        private User author;
        private User assignee;
        private List<User> watchers;

        // Getters and Setters

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class CustomField {
            private int id;
            private String name;
            private String value;
            // Define fields, getters, and setters
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Project {
            private int id;
            private String identifier;
            private String name;
            private String description;
            private String created_on;
            private String homepage;

            // Getters and Setters
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Status {
            private int id;
            private String name;

            // Getters and Setters
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Tracker {
            private int id;
            private String name;

            // Getters and Setters
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Priority {
            private int id;
            private String name;

            // Getters and Setters
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class User {
            private int id;
            private String login;
            private String mail;
            private String firstname;
            private String lastname;
            private String identity_url;
            private String icon_url;

            // Getters and Setters
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Journal {
        private int id;
        private String notes;
        private String created_on;
        private boolean private_notes;
        private Issue.User author;
        private List<Detail> details;

        // Getters and Setters

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Detail {
            private int id;
            private String value;
            private String old_value;
            private String prop_key;
            private String property;

            // Getters and Setters
        }
    }
}

