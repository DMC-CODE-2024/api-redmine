package com.gl.ceir.supportmodule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Issue {
    private int id;
    private Project project;
    private Tracker tracker;
    private Status status;
    private Priority priority;
    private User author;
    private User assigned_to;
    private String subject;
    private String description;
    private String start_date;
    private String due_date;
    private int done_ratio;
    @JsonProperty("is_private")
    private boolean is_private;
    private Float estimated_hours;
    private Float total_estimated_hours;
    private Float spent_hours;
    private Float total_spent_hours;
    private List<Field> custom_fields;
    private String created_on;
    private String updated_on;
    private String closed_on;
    private List<Journal> journals;
    @JsonProperty("attachments")
    private List<Attachment> attachments;
    private List<Upload> uploads;

    @Data
    public static class Project {
        private int id;
        private String name;
    }

    @Data
    public static class Tracker {
        private int id;
        private String name;
    }

    @Data
    public static class Status {
        private int id;
        private String name;
        @JsonProperty("is_closed")
        private Boolean is_closed;
    }

    @Data
    public static class Priority {
        private int id;
        private String name;
    }

    @Data
    public static class User {
        private int id;
        private String name;
    }

    @Data
    public static class Field {
        private int id;
        private String name;
        private String value;
    }

    @Data
    public static class Journal {
        private int id;
        private User user;
        private String notes;
        private String created_on;
        private boolean private_notes;
        private List<Detail> details;
    }

    @Data
    public static class Detail {
        private String property;
        private String name;
        private String old_value;
        private String new_value;
    }

    @Data
    public static class Attachment {
        private Integer id;
        private String filename;
        private Integer filesize;
        private String content_type;
        private String description;
        private String content_url;
        private Author author;
        private Date created_on;
    }

    @Data
    public static class Author {
        private Integer id;
        private String name;
    }

    @Data
    public static class Upload {
        private String token;
        private String fileName;
        private String description;
        private String contentType;
    }
}
