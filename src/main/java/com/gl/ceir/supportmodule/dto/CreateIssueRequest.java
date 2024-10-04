package com.gl.ceir.supportmodule.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateIssueRequest {
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String emailAddress;
    private String category;
    private Integer categoryId;
    private String subject;
    private String description;
    private List<AttachmentRequest> attachments;
    private String raisedBy;
    private String referenceId;
    private Boolean isPrivate;
    private String documentType;
    private String address;
    private String province;
    private String district;
    private String commune;
    private String transactionId;
}
