package com.gl.ceir.supportmodule.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RedmineCreateIssueRequest {
    private Integer projectId;
    private Integer trackerId;
    private Integer statusId;
    private Integer priorityId;
    private String subject;
    private String description;
    private Integer categoryId;
    private String notes;
    private Boolean privateNotes;
    private Boolean isPrivate;
}
