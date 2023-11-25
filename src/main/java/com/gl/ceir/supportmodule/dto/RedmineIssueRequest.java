package com.gl.ceir.supportmodule.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RedmineIssueRequest {
    private RedmineCreateIssueRequest issue;
}
