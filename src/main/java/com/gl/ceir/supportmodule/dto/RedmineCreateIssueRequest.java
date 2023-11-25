package com.gl.ceir.supportmodule.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RedmineCreateIssueRequest {
    private Integer project_id;
    private Integer tracker_id;
    private Integer status_id;
    private Integer priority_id;
    private String subject;
    private List<AttachmentRequest> uploads;
    private String description;
    private Integer category_id;
    private String notes;
    private Boolean private_notes;
    private Boolean is_private;
}
