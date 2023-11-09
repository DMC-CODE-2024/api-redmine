package com.gl.ceir.supportmodule.controller;

import com.gl.ceir.supportmodule.Constants.ClientTypeEnum;
import com.gl.ceir.supportmodule.builder.CreateIssueRequestBuilder;
import com.gl.ceir.supportmodule.client.RedmineClient;
import com.gl.ceir.supportmodule.model.*;
import com.gl.ceir.supportmodule.repository.IssueRepository;
import com.gl.ceir.supportmodule.service.IssuesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    @Autowired
    IssuesService issuesService;


    @Operation(summary = "Get filtered tickets")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))})
    @RequestMapping(path = "/ticket", method = RequestMethod.GET)
    public ResponseEntity<?> getFilteredIssues(
            @Parameter(description = "Start date (yyyy-MM-dd)", example = "2023-12-31", required = false) @RequestParam(required = false) String startDate,
            @Parameter(description = "End date (yyyy-MM-dd)", example = "2023-12-31", required = false) @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String ticketId,
            @RequestParam(required = false) String contactNumber,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String clientType,
            @RequestParam int page,
            @RequestParam int size
    ) {
        try{
            Page<IssuesEntity> pageResponse = issuesService.getFilteredIssues(startDate, endDate, ticketId, contactNumber, status, clientType, page, size);
            List<IssueResponse> issueResponses = pageResponse.getContent().stream()
                    .map(entity -> {
                        int issueId = entity.getIssueId();
                        ClientTypeEnum userType = ClientTypeEnum.valueOf(entity.getUserType());
                        ResponseEntity<IssueResponse> resp = redmineClient.getIssueWithJournals(issueId, userType, entity);
                        return CreateIssueRequestBuilder.issueResponse(resp.getBody().getIssue(), entity);
                    })
                    .collect(Collectors.toList());
            PaginatedResponse response = PaginatedResponse.builder().data(issueResponses).currentPage(page).totalPages(pageResponse.getTotalPages()).totalElements(pageResponse.getTotalElements()).numberOfElements(pageResponse.getNumberOfElements()).build();
            return ResponseEntity.ok(response);
        } catch (Exception ex){
            log.error("Exception while fetching filtered issues, {}",ex.getMessage());
            return new ResponseEntity<>(ErrorResponse.builder().message(ex.getMessage()).errorCode(HttpStatus.BAD_REQUEST.value()).build(), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Get issue by ticketId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Server Error", content = @Content)})
    @RequestMapping(path = "/ticket/{ticketId}", method = RequestMethod.GET)
    public ResponseEntity<IssueResponse> getIssueById(@PathVariable String ticketId) {
        try {
            ClientTypeEnum clientType = ClientInfo.getClientType();
            Optional<IssuesEntity> issuesEntity = issueRepository.findByTicketId(ticketId);
            if (issuesEntity.isPresent()){
                return redmineClient.getIssueWithJournals(issuesEntity.get().getIssueId(), clientType, issuesEntity.get());
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
    @RequestMapping(path = "/ticket/msisdn/{msisdn}", method = RequestMethod.GET)
    public ResponseEntity<List<IssueResponse>> getIssueByMsisdn(@PathVariable String msisdn) {
        try {
            List<IssuesEntity> issuesEntity = issueRepository.findByMsisdn(msisdn);
            List<IssueResponse> issueResponses = issuesEntity.stream()
                    .map(entity -> {
                        int issueId = entity.getIssueId();
                        ClientTypeEnum clientType = ClientTypeEnum.valueOf(entity.getUserType());
                        ResponseEntity<IssueResponse> resp = redmineClient.getIssueWithJournals(issueId, clientType, entity);
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
    @RequestMapping(path = "/ticket", method = RequestMethod.POST)
    public ResponseEntity<IssueResponse> createIssue(@RequestBody CreateIssueRequest createIssueRequest) {
        ClientTypeEnum clientType = ClientInfo.getClientType();
        return redmineClient.createIssue(createIssueRequest, clientType);
    }

//    @Operation(summary = "Update Issue by ticketId")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "OK"),
//            @ApiResponse(responseCode = "404", description = "Not Found"),
//            @ApiResponse(responseCode = "400", description = "Bad Request"),
//            @ApiResponse(responseCode = "500", description = "Server Error")})
//    @RequestMapping(path = "/ticket/{ticketId}", method = RequestMethod.PUT)
//    public ResponseEntity<Void> updateIssue(@PathVariable String ticketId, @RequestBody RedmineIssueRequest createRedmineIssueRequest) {
//        ClientTypeEnum clientType = ClientInfo.getClientType();
//        Optional<IssuesEntity> issuesEntity = issueRepository.findByTicketId(ticketId);
//        if (issuesEntity.isPresent()){
//            return redmineClient.updateIssue(issuesEntity.get(), createRedmineIssueRequest, clientType);
//        } else {
//            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
//        }
//    }

    @Operation(summary = "Add notes/comments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Server Error")})
    @RequestMapping(path = "/ticket/{ticketId}/notes", method = RequestMethod.PUT)
    public ResponseEntity<Void> addNotes(@PathVariable String ticketId, @RequestBody CreateNotesRequest createIssueRequest) {
        ClientTypeEnum clientType = ClientInfo.getClientType();
        Optional<IssuesEntity> issuesEntity = issueRepository.findByTicketId(ticketId);
        if (issuesEntity.isPresent()){
            RedmineIssueRequest createRedmineIssueRequest = CreateIssueRequestBuilder.addNotes(createIssueRequest, issuesEntity.get());
            return redmineClient.updateIssue(issuesEntity.get(), createRedmineIssueRequest, clientType, false);
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
    @RequestMapping(path = "/ticket/resolve/{ticketId}", method = RequestMethod.PUT)
    public ResponseEntity<Void> resolveIssue(@PathVariable String ticketId) {
        ClientTypeEnum clientType = ClientInfo.getClientType();
        Optional<IssuesEntity> issuesEntity = issueRepository.findByTicketId(ticketId);
        if (issuesEntity.isPresent()){
            if(clientType.equals(ClientTypeEnum.REGISTERED) || (clientType.equals(ClientTypeEnum.END_USER) && issuesEntity.get().getUserId().equals(ClientInfo.getClientId()))) {
                RedmineIssueRequest createRedmineIssueRequest = CreateIssueRequestBuilder.resolveIssue(resolveStatusId);
                return redmineClient.updateIssue(issuesEntity.get(), createRedmineIssueRequest, clientType, true);
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
    @RequestMapping(path = "/ticket/attachment/upload", method = RequestMethod.POST)
    public ResponseEntity<UploadResponse> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            ClientTypeEnum clientType = ClientInfo.getClientType();
            byte[] fileContent = file.getBytes();
            String fileName = file.getOriginalFilename();
            return redmineClient.uploadFile(fileName, fileContent, clientType);
        } catch (IOException e) {
            log.error("exception in uploading media: {}", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Rate/Add feedback for issue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Server Error", content = @Content)})
    @RequestMapping(path = "/ticket/{ticketId}/rate", method = RequestMethod.POST)
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
            log.error("exception in rate/feedback api: {}", ex);
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
