package com.gl.ceir.supportmodule.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RedmineIssueRequest {
    private RedmineCreateIssueRequest issue;
}
