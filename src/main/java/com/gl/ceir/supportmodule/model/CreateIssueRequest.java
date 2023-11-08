package com.gl.ceir.supportmodule.model;

import com.gl.ceir.supportmodule.Constants.ClientTypeEnum;
import lombok.Data;
import java.util.List;

@Data
public class CreateIssueRequest {
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String emailAddress;
    private String category;
    private String subject;
    private String description;
    private List<AttachmentRequest> attachments;
    private String raisedBy;
    private Boolean isPrivate;
}
