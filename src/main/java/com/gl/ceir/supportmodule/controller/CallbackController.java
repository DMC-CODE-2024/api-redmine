package com.gl.ceir.supportmodule.controller;

import com.gl.ceir.supportmodule.config.RedmineConfiguration;
import com.gl.ceir.supportmodule.dto.WebhookPayload;
import com.gl.ceir.supportmodule.model.app.IssuesEntity;
import com.gl.ceir.supportmodule.repository.app.DuplicateDeviceDetailRepository;
import com.gl.ceir.supportmodule.service.IssuesService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class CallbackController {
    private final Logger log = LogManager.getLogger(getClass());

    @Autowired
    IssuesService issuesService;
    @Autowired
    RedmineConfiguration redmineConfiguration;
    @Autowired
    private DuplicateDeviceDetailRepository duplicateDeviceDetailRepository;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody WebhookPayload payload) {
        try {
            // Extract issue ID and status from the payload
            int payloadIssueId = payload.getPayload().getIssue().getId();
            int trackerId = payload.getPayload().getIssue().getTracker().getId();
            String payloadStatus = payload.getPayload().getIssue().getStatus().getName();

            // Find the issue by ID in the database
            Optional<IssuesEntity> optionalIssue = issuesService.findByIssueId(payloadIssueId);

            if (optionalIssue.isPresent()) {
                IssuesEntity issue = optionalIssue.get();
                String currentStatus = issue.getStatus();

                // Check if the status is different
                if (!currentStatus.equals(payloadStatus)) {
                    // Update the status in the database
                    issue.setStatus(payloadStatus);
                    issuesService.updateIssueStatus(payloadIssueId, payloadStatus);
                    if (redmineConfiguration.getClosedStatusName().equals(payloadStatus) && redmineConfiguration.getDuplicateTrackerId() == trackerId) {
                        String resolvedBy = payload.getPayload().getJournal().getAuthor().getLogin();
                        int rowsUpdated = duplicateDeviceDetailRepository.updateStatusAndUpdatedByByRedmineTktId("ORIGINAL", resolvedBy, String.valueOf(payloadIssueId));
                        if (rowsUpdated > 0) {
                            log.info("Duplicate device detail status updated: ORIGINAL. Payload: " + payload);
                        }
                    }
                    log.info("Issue status updated: " + payloadIssueId + " -> " + payloadStatus);
                } else {
                    log.info("Issue status unchanged: " + payloadIssueId);
                }
            } else {
                log.info("Issue not found: " + payloadIssueId);
            }

            // Return a response
            return new ResponseEntity<>("Webhook processed", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<>("Webhook processing failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
