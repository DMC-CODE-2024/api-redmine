package com.gl.ceir.supportmodule.controller;

import com.gl.ceir.supportmodule.Constants.ClientTypeEnum;
import com.gl.ceir.supportmodule.builder.CreateIssueRequestBuilder;
import com.gl.ceir.supportmodule.client.RedmineClient;
import com.gl.ceir.supportmodule.config.RedmineConfiguration;
import com.gl.ceir.supportmodule.dto.*;
import com.gl.ceir.supportmodule.enums.RedmineStatusEnum;
import com.gl.ceir.supportmodule.model.app.IssuesEntity;
import com.gl.ceir.supportmodule.repository.app.GenericRepository;
import com.gl.ceir.supportmodule.repository.app.IssueRepository;
import com.gl.ceir.supportmodule.model.redmine.IssueStatusCounts;
import com.gl.ceir.supportmodule.repository.redmine.RedmineGenericRepository;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MainController {
    private final Logger log = LogManager.getLogger(getClass());
    @Autowired
    RedmineConfiguration redmineConfiguration;
    @Autowired
    RedmineClient redmineClient;
    @Autowired
    ClientInfo clientInfo;
    @Autowired
    IssueRepository issueRepository;
    @Autowired
    IssuesService issuesService;
    @Autowired
    @Qualifier("appRepository")
    GenericRepository genericRepository;
    @Autowired
    @Qualifier("redmineRepository")
    RedmineGenericRepository redmineGenericRepository;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }


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
            @RequestParam(required = false) String raisedBy,
            @RequestParam int page,
            @RequestParam int size
    ) {
        try {
            Page<IssuesEntity> pageResponse = issuesService.getFilteredIssues(startDate, endDate, ticketId, contactNumber, status, clientType, page, size, raisedBy);
            List<IssueResponse> issueResponses = pageResponse.getContent().stream()
                    .map(entity -> {
                        try {
                            int issueId = entity.getIssueId();
                            ClientTypeEnum userType = ClientTypeEnum.valueOf(entity.getUserType());
                            ResponseEntity<IssueResponse> resp = redmineClient.getIssueWithJournals(issueId, userType, entity);
                            // Check if getBody() is not null before calling getIssue()
                            if (resp.getBody() != null && resp.getBody().getIssue() != null) {
                                return CreateIssueRequestBuilder.issueResponse(resp.getBody().getIssue(), entity);
                            } else {
                                return null;
                            }
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            PaginatedResponse response = PaginatedResponse.builder().data(issueResponses).currentPage(page).totalPages(pageResponse.getTotalPages()).totalElements(pageResponse.getTotalElements()).numberOfElements(pageResponse.getNumberOfElements()).build();
            return ResponseEntity.ok(response);
        } catch (RuntimeException rex) {
            return new ResponseEntity<>(ErrorResponse.builder().message(rex.getMessage()).errorCode(HttpStatus.BAD_REQUEST.value()).build(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex){
            log.error("Exception while fetching filtered issues, {}",ex.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get issue by ticketId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = IssueResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Server Error", content = @Content)})
    @RequestMapping(path = "/ticket/{ticketId}", method = RequestMethod.GET)
    public ResponseEntity<?> getIssueById(@PathVariable String ticketId) {
        try {
            ClientTypeEnum clientType = ClientInfo.getClientType();
            Optional<IssuesEntity> issuesEntity = issueRepository.findByTicketId(ticketId);
            if (issuesEntity.isPresent()) {
                return redmineClient.getIssueWithJournals(issuesEntity.get().getIssueId(), clientType, issuesEntity.get());
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException rex) {
            return new ResponseEntity<>(ErrorResponse.builder().message(rex.getMessage()).errorCode(HttpStatus.BAD_REQUEST.value()).build(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error(e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get issue by msisdn")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PaginatedResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Server Error", content = @Content)})
    @RequestMapping(path = "/ticket/msisdn/{msisdn}", method = RequestMethod.GET)
    public ResponseEntity<?> getIssueByMsisdn(
            @PathVariable String msisdn,
            @RequestParam(value = "page", defaultValue = "0") @Parameter(description = "Page number") int page,
            @RequestParam(value = "size", defaultValue = "10") @Parameter(description = "Page size") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<IssuesEntity> issuesEntityPage = issueRepository.findByMsisdn(msisdn, pageable);
            List<IssueResponse> issueResponses = issuesEntityPage.getContent().stream()
                    .map(entity -> {
                        int issueId = entity.getIssueId();
                        ClientTypeEnum clientType = ClientTypeEnum.valueOf(entity.getUserType());
                        ResponseEntity<IssueResponse> resp = redmineClient.getIssueWithJournals(issueId, clientType, entity);
                        return CreateIssueRequestBuilder.issueResponse(resp.getBody().getIssue(), entity);
                    })
                    .collect(Collectors.toList());

            PaginatedResponse<IssueResponse> response = new PaginatedResponse<>();
            response.setData(issueResponses);
            response.setCurrentPage(issuesEntityPage.getNumber());
            response.setTotalPages(issuesEntityPage.getTotalPages());
            response.setNumberOfElements(issuesEntityPage.getNumberOfElements());
            response.setTotalElements(issuesEntityPage.getTotalElements());

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException rex) {
            return new ResponseEntity<>(ErrorResponse.builder().message(rex.getMessage()).errorCode(HttpStatus.BAD_REQUEST.value()).build(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error(e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Create Issue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = IssueResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Server Error", content = @Content)})
    @RequestMapping(path = "/ticket", method = RequestMethod.POST)
    public ResponseEntity<?> createIssue(@RequestBody CreateIssueRequest createIssueRequest) {
        try {
            ClientTypeEnum clientType = ClientInfo.getClientType();
            return redmineClient.createIssue(createIssueRequest, clientType);
        } catch (RuntimeException rex) {
            return new ResponseEntity<>(ErrorResponse.builder().message(rex.getMessage()).errorCode(HttpStatus.BAD_REQUEST.value()).build(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error(e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
            @ApiResponse(responseCode = "200", description = "OK", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Server Error")})
    @RequestMapping(path = "/ticket/{ticketId}/notes", method = RequestMethod.PUT)
    public ResponseEntity<?> addNotes(@PathVariable String ticketId, @RequestBody CreateNotesRequest createIssueRequest) {
        try {
            ClientTypeEnum clientType = ClientInfo.getClientType();
            Optional<IssuesEntity> issuesEntity = issueRepository.findByTicketId(ticketId);
            if (issuesEntity.isPresent()){
                RedmineIssueRequest createRedmineIssueRequest = CreateIssueRequestBuilder.addNotes(createIssueRequest, issuesEntity.get());
                return redmineClient.updateIssue(issuesEntity.get(), createRedmineIssueRequest, clientType, false, null);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException rex) {
            return new ResponseEntity<>(ErrorResponse.builder().message(rex.getMessage()).errorCode(HttpStatus.BAD_REQUEST.value()).build(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error(e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Mark issue as Resolved/Closed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Server Error")})
    @RequestMapping(path = "/ticket/{status}/{ticketId}", method = RequestMethod.PUT)
    public ResponseEntity<?> updateIssueStatus(
            @Parameter(description = "Issue status", example = "{RESOLVED, CLOSED}", required = true)
            @PathVariable String status,
            @PathVariable String ticketId) {
        try {
            RedmineStatusEnum redmineStatusEnum = RedmineStatusEnum.valueOf(status);
            ClientTypeEnum clientType = ClientInfo.getClientType();
            String test = ClientInfo.getClientId();
            Optional<IssuesEntity> issuesEntity = issueRepository.findByTicketId(ticketId);
            if (issuesEntity.isPresent()){
                if(clientType.equals(ClientTypeEnum.REGISTERED) || (clientType.equals(ClientTypeEnum.END_USER) && issuesEntity.get().getUserId().equals(ClientInfo.getClientId()))) {
                    RedmineIssueRequest createRedmineIssueRequest = CreateIssueRequestBuilder.resolveIssue(redmineConfiguration.getResolvedStatusId());
                    return redmineClient.updateIssue(issuesEntity.get(), createRedmineIssueRequest, clientType, true, redmineStatusEnum);
                } else {
                    return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException rex) {
            return new ResponseEntity<>(ErrorResponse.builder().message(rex.getMessage()).errorCode(HttpStatus.BAD_REQUEST.value()).build(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Upload Attachments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UploadResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Server Error", content = @Content)})
    @RequestMapping(path = "/ticket/attachment/upload", method = RequestMethod.POST)
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            ClientTypeEnum clientType = ClientInfo.getClientType();
            byte[] fileContent = file.getBytes();
            String fileName = file.getOriginalFilename();
            return redmineClient.uploadFile(fileName, fileContent, clientType);
        } catch (RuntimeException rex) {
            return new ResponseEntity<>(ErrorResponse.builder().message(rex.getMessage()).errorCode(HttpStatus.BAD_REQUEST.value()).build(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("exception in uploading media: {}", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Rate/Add feedback for issue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Server Error", content = @Content)})
    @RequestMapping(path = "/ticket/{ticketId}/rate", method = RequestMethod.POST)
    public ResponseEntity<String> rateIssue(@PathVariable String ticketId, @RequestBody FeedbackRequest feedbackRequest) {
        try {
            Optional<IssuesEntity> issuesEntity = issueRepository.findByTicketId(ticketId);
            if (issuesEntity.isPresent()){
                issuesEntity.get().setRating(feedbackRequest.getRatings());
                issuesEntity.get().setFeedback(feedbackRequest.getFeedback());
                issueRepository.save(issuesEntity.get());
                return new ResponseEntity<>("Thanks for the feedback", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Invalid ticket id", HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException rex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(rex.getMessage());
        } catch (Exception ex) {
            log.error("exception in rate/feedback api: {}", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Redmine api for testing
    @RequestMapping(path = "/redmine-issue-id/{issueId}", method = RequestMethod.GET)
    public ResponseEntity<RedmineResponse> getRedmineIssueById(@PathVariable String issueId) {
        try {
            ClientTypeEnum clientType = ClientTypeEnum.END_USER;
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

    @Operation(summary = "Dashboard API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Server Error", content = @Content)})
    @RequestMapping(path = "/dashboard", method = RequestMethod.GET)
    public ResponseEntity<?> getDashboard() {
        try {
            if(ClientTypeEnum.END_USER == ClientInfo.getClientType()) {
                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            }
            Optional<String> authorId = redmineGenericRepository.getAuthorId(ClientInfo.getClientId());
            if(!authorId.isPresent()) {
                throw new RuntimeException("User does not exist");
            }
            IssueStatusCounts myDashboard = redmineGenericRepository.getIssueStatusCounts(authorId.get(), redmineConfiguration.getCreatedStatusId(), redmineConfiguration.getProgressStatusId(), redmineConfiguration.getClosedStatusId(), redmineConfiguration.getResolvedStatusId(), redmineConfiguration.getProjectId());
            IssueStatusCounts allDashBoard = redmineGenericRepository.getIssueStatusCountsForProject(redmineConfiguration.getProjectId(),redmineConfiguration.getCreatedStatusId(), redmineConfiguration.getProgressStatusId(), redmineConfiguration.getClosedStatusId(), redmineConfiguration.getResolvedStatusId());
            DashboardResponse response = DashboardResponse.builder().myDashboard(myDashboard).allDashboard(allDashBoard).build();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException rex) {
            return new ResponseEntity<>(ErrorResponse.builder().message(rex.getMessage()).errorCode(HttpStatus.BAD_REQUEST.value()).build(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error(e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get Issue Categories by Project ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Server Error", content = @Content)
    })
    @GetMapping("/projects/issue_categories")
    public ResponseEntity<List<CategoryResponse>> getIssueCategories() {
        try {
            ClientTypeEnum clientType = ClientInfo.getClientType();

            ResponseEntity<List<CategoryResponse>> response = redmineClient.getIssueCategories(redmineConfiguration.getProjectId(), clientType);
            return response;
        } catch (RuntimeException rex) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
