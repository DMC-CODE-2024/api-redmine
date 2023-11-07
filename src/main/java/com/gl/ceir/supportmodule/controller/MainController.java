package com.gl.ceir.supportmodule.controller;

import com.gl.ceir.supportmodule.Constants.ClientTypeEnum;
import com.gl.ceir.supportmodule.builder.CreateIssueRequestBuilder;
import com.gl.ceir.supportmodule.client.RedmineClient;
import com.gl.ceir.supportmodule.model.*;
import com.gl.ceir.supportmodule.repository.IssueRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MainController {
    private final Logger log = LogManager.getLogger(getClass());
    @Value("${redmine-resolve-status-id}")
    private int resolveStatusId;
    @Autowired
    RedmineClient redmineClient;
    @Autowired
    ClientInfo clientInfo;
    @Autowired
    IssueRepository issueRepository;

    @Operation(summary = "Get issue by ticketId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Server Error", content = @Content)})
    @RequestMapping(path = "/issue/{ticketId}", method = RequestMethod.GET)
    public ResponseEntity<IssueResponse> getIssueById(@PathVariable String ticketId) {
        try {
            ClientTypeEnum clientType = ClientInfo.getClientType();
            Optional<IssuesEntity> issuesEntity = issueRepository.findByTicketId(ticketId);
            if (issuesEntity.isPresent()){
                return redmineClient.getIssueWithJournals(issuesEntity.get().getTicketId(), clientType, issuesEntity.get());
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (HttpClientErrorException.NotFound e) {
            log.error(e);
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error(e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get issue by msisdn")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Server Error", content = @Content)})
    @RequestMapping(path = "/issue/msisdn/{msisdn}", method = RequestMethod.GET)
    public ResponseEntity<List<IssueResponse>> getIssueByMsisdn(@PathVariable String msisdn) {
        try {
            List<IssuesEntity> issuesEntity = issueRepository.findByMsisdn(msisdn);
            List<IssueResponse> issueResponses = issuesEntity.stream()
                    .map(entity -> {
                        String ticketId = entity.getTicketId();
                        ClientTypeEnum clientType = entity.getUserType();
                        ResponseEntity<IssueResponse> resp = redmineClient.getIssueWithJournals(ticketId, clientType, entity);
                        return CreateIssueRequestBuilder.issueResponse(resp.getBody().getIssue(), entity);
                    })
                    .collect(Collectors.toList());
            return new ResponseEntity<>(issueResponses, HttpStatus.OK);
        } catch (HttpClientErrorException.NotFound e) {
            log.error(e);
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error(e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Create Issue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Server Error", content = @Content)})
    @RequestMapping(path = "/issue", method = RequestMethod.POST)
    public ResponseEntity<IssueResponse> createIssue(@RequestBody CreateIssueRequest createIssueRequest) {
        ClientTypeEnum clientType = ClientInfo.getClientType();
        return redmineClient.createIssue(createIssueRequest, clientType);
    }

    @Operation(summary = "Update Issue by ticketId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Server Error")})
    @RequestMapping(path = "/issue/{ticketId}", method = RequestMethod.PUT)
    public ResponseEntity<Void> updateIssue(@PathVariable String ticketId, @RequestBody RedmineIssueRequest createRedmineIssueRequest) {
        ClientTypeEnum clientType = ClientInfo.getClientType();
        Optional<IssuesEntity> issuesEntity = issueRepository.findByTicketId(ticketId);
        if (issuesEntity.isPresent()){
            return redmineClient.updateIssue(issuesEntity.get().getIssueId(), createRedmineIssueRequest, clientType);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Add notes/comments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Server Error")})
    @RequestMapping(path = "/issue/{ticketId}/notes", method = RequestMethod.PUT)
    public ResponseEntity<Void> addNotes(@PathVariable String ticketId, @RequestBody CreateIssueRequest createIssueRequest) {
        ClientTypeEnum clientType = ClientInfo.getClientType();
        Optional<IssuesEntity> issuesEntity = issueRepository.findByTicketId(ticketId);
        if (issuesEntity.isPresent()){
            RedmineIssueRequest createRedmineIssueRequest = CreateIssueRequestBuilder.addNotes(createIssueRequest, issuesEntity.get());
            return redmineClient.updateIssue(issuesEntity.get().getIssueId(), createRedmineIssueRequest, clientType);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Mark issue as Resolved")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Server Error")})
    @RequestMapping(path = "/issue/resolve/{ticketId}", method = RequestMethod.PUT)
    public ResponseEntity<Void> resolveIssue(@PathVariable String ticketId) {
        ClientTypeEnum clientType = ClientInfo.getClientType();
        Optional<IssuesEntity> issuesEntity = issueRepository.findByTicketId(ticketId);
        if (issuesEntity.isPresent()){
            if(clientType.equals(ClientTypeEnum.REGISTERED) || (clientType.equals(ClientTypeEnum.UNREGISTERED) && issuesEntity.get().getUserId().equals(ClientInfo.getClientId()))) {
                RedmineIssueRequest createRedmineIssueRequest = CreateIssueRequestBuilder.resolveIssue(resolveStatusId);
                return redmineClient.updateIssue(issuesEntity.get().getIssueId(), createRedmineIssueRequest, clientType);
            } else {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Upload Attachments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Server Error", content = @Content)})
    @RequestMapping(path = "/issue/attachment/upload", method = RequestMethod.POST)
    public ResponseEntity<UploadResponse> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            ClientTypeEnum clientType = ClientInfo.getClientType();
            byte[] fileContent = file.getBytes();
            String fileName = file.getOriginalFilename();
            return redmineClient.uploadFile(fileName, fileContent, clientType);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Rate/Add feedback for issue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Server Error", content = @Content)})
    @RequestMapping(path = "/issue/{ticketId}/rate", method = RequestMethod.POST)
    public ResponseEntity<String> rateIssue(@PathVariable String ticketId, @RequestBody FeedbackRequest feedbackRequest) {
        try {
            Optional<IssuesEntity> issuesEntity = issueRepository.findByTicketId(ticketId);
            if (issuesEntity.isPresent()){
                issuesEntity.get().setRating(feedbackRequest.getRatings());
                issuesEntity.get().setFeedback(feedbackRequest.getFeedback());
                return new ResponseEntity<>("Thanks for the feedback", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Invalid ticket id", HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Redmine api for testing
    @RequestMapping(path = "/redmine-issue/{issueId}", method = RequestMethod.GET)
    public ResponseEntity<RedmineResponse> getRedmineIssueById(@PathVariable String issueId) {
        try {
            ClientTypeEnum clientType = ClientInfo.getClientType();
            return redmineClient.getRedmineIssueWithJournals(issueId, clientType);
        } catch (HttpClientErrorException.NotFound e) {
            log.error(e);
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error(e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // analytics


}
