package com.gl.ceir.supportmodule.builder;

import com.gl.ceir.supportmodule.dto.*;
import com.gl.ceir.supportmodule.model.app.IssuesEntity;

public class CreateIssueRequestBuilder {
    public static RedmineIssueRequest redmineCreateIssueRequest(CreateIssueRequest createIssueRequest, int projectId, int tracker, int createIssueId) {

        RedmineCreateIssueRequest issue = RedmineCreateIssueRequest.builder()
                .project_id(projectId).tracker_id(tracker)
                .subject(createIssueRequest.getSubject())
                .description(createIssueRequest.getDescription())
                .status_id(createIssueId)
                .is_private(createIssueRequest.getIsPrivate())
                .uploads(createIssueRequest.getAttachments())
                .build();
        return RedmineIssueRequest.builder().issue(issue).build();
    }

    public static IssuesEntity saveToDb(CreateIssueRequest req, int redmineId, String status, String userType, String userId) {
        return IssuesEntity.builder()
                .email(req.getEmailAddress())
                .category(req.getCategory())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .isPrivate(req.getIsPrivate())
                .msisdn(req.getMobileNumber())
                .status(status)
                .subject(req.getSubject())
                .issueId(redmineId)
                .userId(userId)
                .userType(userType)
                .raisedBy(req.getRaisedBy())
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
                .raisedBy(issue.getRaisedBy())
                .userId(issue.getUserId())
                .userType(issue.getUserType())
                .build();
    }

    public static RedmineIssueRequest addNotes(CreateNotesRequest createIssueRequest, IssuesEntity issuesEntity) {
        RedmineCreateIssueRequest issue = RedmineCreateIssueRequest.builder()
                .notes(createIssueRequest.getNotes())
                .private_notes(createIssueRequest.getPrivateNotes())
                .build();
        return RedmineIssueRequest.builder().issue(issue).build();
    }

    public static RedmineIssueRequest resolveIssue(int statusId) {
        RedmineCreateIssueRequest issue = RedmineCreateIssueRequest.builder()
                .status_id(statusId)
                .build();
        return RedmineIssueRequest.builder().issue(issue).build();
    }
}
