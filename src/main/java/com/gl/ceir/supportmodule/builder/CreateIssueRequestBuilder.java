package com.gl.ceir.supportmodule.builder;

import com.gl.ceir.supportmodule.model.*;

public class CreateIssueRequestBuilder {
    public static RedmineIssueRequest redmineCreateIssueRequest(CreateIssueRequest createIssueRequest, int projectId, int tracker) {

        RedmineCreateIssueRequest issue = RedmineCreateIssueRequest.builder()
                .projectId(projectId).trackerId(tracker)
                .subject(createIssueRequest.getSubject())
                .description(createIssueRequest.getDescription())
                .isPrivate(createIssueRequest.getIsPrivate())
                .build();
        return RedmineIssueRequest.builder().issue(issue).build();
    }

    public static IssuesEntity saveToDb(CreateIssueRequest req, int redmineId) {
        return IssuesEntity.builder()
                .email(req.getEmailAddress())
                .category(req.getCategory())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .isPrivate(req.getIsPrivate())
                .msisdn(req.getMobileNumber())
//                .status(issue.getStatus().getName())
                .subject(req.getSubject())
                .issueId(redmineId)
                .build();
    }

    public static IssueResponse issueResponse(Issue issueResponse, IssuesEntity issue) {
        return IssueResponse.builder()
                .ticketId(issue.getTicketId())
                .firstName(issue.getFirstName())
                .lastName(issue.getLastName())
                .category(issue.getCategory())
                .issue(issueResponse)
                .emailAddress(issue.getEmail())
                .mobileNumber(issue.getMsisdn())
                .isPrivate(issue.getIsPrivate())
                .raisedBy(issue.getCreatedBy())
                .userId(issue.getUserId())
                .userType(issue.getUserType())
                .build();
    }

    public static RedmineIssueRequest addNotes(CreateIssueRequest createIssueRequest, IssuesEntity issuesEntity) {
        RedmineCreateIssueRequest issue = RedmineCreateIssueRequest.builder()
                .notes(createIssueRequest.getNotes())
                .privateNotes(createIssueRequest.getPrivateNotes())
                .build();
        return RedmineIssueRequest.builder().issue(issue).build();
    }

    public static RedmineIssueRequest resolveIssue(int statusId) {
        RedmineCreateIssueRequest issue = RedmineCreateIssueRequest.builder()
                .statusId(statusId)
                .build();
        return RedmineIssueRequest.builder().issue(issue).build();
    }
}
