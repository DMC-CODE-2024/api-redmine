package com.gl.ceir.supportmodule.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gl.ceir.supportmodule.Constants.ClientTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IssueResponse {
    private String ticketId;
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String emailAddress;
    private String category;
    private String userId;
    private String userType;
    private String raisedBy;
    private Issue issue;
    private Boolean isPrivate;
}
