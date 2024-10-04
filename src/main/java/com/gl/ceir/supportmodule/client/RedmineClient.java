package com.gl.ceir.supportmodule.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gl.ceir.supportmodule.Constants.ClientTypeEnum;
import com.gl.ceir.supportmodule.builder.CreateIssueRequestBuilder;
import com.gl.ceir.supportmodule.config.RedmineConfiguration;
import com.gl.ceir.supportmodule.dto.*;
import com.gl.ceir.supportmodule.enums.RedmineStatusEnum;
import com.gl.ceir.supportmodule.model.app.DuplicateDeviceDetail;
import com.gl.ceir.supportmodule.model.app.IssuesEntity;
import com.gl.ceir.supportmodule.repository.app.DuplicateDeviceDetailRepository;
import com.gl.ceir.supportmodule.repository.app.GenericRepository;
import com.gl.ceir.supportmodule.repository.app.IssueRepository;
import com.gl.ceir.supportmodule.repository.redmine.RedmineGenericRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class RedmineClient {
    private final Logger log = LogManager.getLogger(getClass());
    @Value("${redmine.registered-user-api-key}")
    private String registeredUserApiKey;
    @Value("${custom_field_id_address}")
    private Integer cfAddress;
    @Value("${custom_field_id_province}")
    private Integer cfProvince;
    @Value("${custom_field_id_district}")
    private Integer cfDistrict;
    @Value("${custom_field_id_commune}")
    private Integer cfCommune;
    @Autowired
    RedmineConfiguration redmineConfiguration;
    @Autowired
    private IssueRepository issueRepository;
    @Autowired
    private GenericRepository genericRepository;
    @Autowired
    private RedmineGenericRepository redmineGenericRepository;
    @Autowired
    private DuplicateDeviceDetailRepository duplicateDeviceDetailRepository;

    private final RestTemplate restTemplate;

    public RedmineClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<IssueResponse> getIssueWithJournals(int issueId, ClientTypeEnum clientType, IssuesEntity issuesEntity) {
        String key = getClientApiKey(clientType);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("X-Redmine-API-Key", key);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(redmineConfiguration.getBaseUrl() + "/issues/" + issueId + ".json?include=journals,attachments", HttpMethod.GET, requestEntity, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            RedmineResponse issue = objectMapper.readValue(responseEntity.getBody(), RedmineResponse.class);
            Collections.sort(issue.getIssue().getJournals(),
                    (j1, j2) -> j2.getCreated_on().compareTo(j1.getCreated_on()));
            IssueResponse issueResponse = CreateIssueRequestBuilder.issueResponse(issue.getIssue(), issuesEntity);
            return new ResponseEntity<>(issueResponse, responseEntity.getStatusCode());
        } catch (Exception e) {
            log.error("exception while fetching issue for id: {}, ex: {}", issueId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<IssueResponse> createIssue(CreateIssueRequest createIssueRequest, ClientTypeEnum clientType) {
        String key = getClientApiKey(clientType);
        try {
            RedmineIssueRequest createRedmineIssueRequest = CreateIssueRequestBuilder.redmineCreateIssueRequest(createIssueRequest, redmineConfiguration.getProjectId(), redmineConfiguration.getTrackerId(), redmineConfiguration.getCreatedStatusId());
            if(createIssueRequest.getCategory() != null && createIssueRequest.getCategoryId() != null) {
                createRedmineIssueRequest.getIssue().setCategory_id(createIssueRequest.getCategoryId());
                if(createIssueRequest.getCategory().equalsIgnoreCase("Duplicate")) {
                    ArrayList<Issue.Field> customFields = new ArrayList<>();
                    createRedmineIssueRequest.getIssue().setTracker_id(redmineConfiguration.getDuplicateTrackerId());
                    if(cfAddress != null && createIssueRequest.getAddress() != null) {
                        customFields.add(Issue.Field.builder().id(cfAddress).name("a").value(createIssueRequest.getAddress()).build());
                    }
                    if(cfDistrict != null && createIssueRequest.getDistrict() != null) {
                        customFields.add(Issue.Field.builder().id(cfDistrict).name("district").value(createIssueRequest.getDistrict()).build());
                    }
                    if(cfCommune != null && createIssueRequest.getCommune() != null) {
                        customFields.add(Issue.Field.builder().id(cfCommune).name("commune").value(createIssueRequest.getCommune()).build());
                    }
                    if(cfProvince != null && createIssueRequest.getProvince() != null) {
                        customFields.add(Issue.Field.builder().id(cfProvince).name("province").value(createIssueRequest.getProvince()).build());
                    }
                    if(!customFields.isEmpty()){
                        createRedmineIssueRequest.getIssue().setCustom_fields(customFields);
                    }
                }
            }
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("X-Redmine-API-Key", key);

            HttpEntity<RedmineIssueRequest> requestEntity = new HttpEntity<>(createRedmineIssueRequest, headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(redmineConfiguration.getBaseUrl() + "/issues.json", HttpMethod.POST, requestEntity, String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                RedmineResponse createdIssue = mapper.readValue(responseEntity.getBody(), RedmineResponse.class);
                IssuesEntity issue = issueRepository.save(CreateIssueRequestBuilder.saveToDb(createIssueRequest, createdIssue.getIssue().getId(), redmineConfiguration.getCreatedStatusName(), clientType.name(), ClientInfo.getClientId()));
                if (createIssueRequest.getTransactionId() == null) {
                    throw new Exception("Transaction Id null in the request");
                }
                if(createIssueRequest.getCategory() != null && createIssueRequest.getCategory().equalsIgnoreCase("Duplicate")) {
                    Optional<DuplicateDeviceDetail> duplicateDeviceDetailOptional = duplicateDeviceDetailRepository.findByTransactionId(createIssueRequest.getTransactionId());
                    if(duplicateDeviceDetailOptional.isPresent()) {
                        DuplicateDeviceDetail duplicateDeviceDetail = duplicateDeviceDetailOptional.get();
                        duplicateDeviceDetail.setRedmineTktId(String.valueOf(createdIssue.getIssue().getId()));
                        duplicateDeviceDetailRepository.save(duplicateDeviceDetail);
                    }
                }
                return new ResponseEntity<>(CreateIssueRequestBuilder.issueResponse(createdIssue.getIssue(), issue), HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(new IssueResponse(), responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            log.error("exception in creating issue: {}", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Void> updateIssue(IssuesEntity issuesEntity, RedmineIssueRequest updatedIssue, ClientTypeEnum clientType, boolean isResolvedOrClosed, RedmineStatusEnum status) {
        String key = getClientApiKey(clientType);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("X-Redmine-API-Key", key);

            HttpEntity<RedmineIssueRequest> requestEntity = new HttpEntity<>(updatedIssue, headers);

            ResponseEntity<Void> responseEntity = restTemplate.exchange(
                    redmineConfiguration.getBaseUrl() + "/issues/" + issuesEntity.getIssueId() + ".json",
                    HttpMethod.PUT,
                    requestEntity,
                    Void.class
            );

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                if(isResolvedOrClosed) {
                    switch (status) {
                        case RESOLVED:
                            issuesEntity.setStatus(redmineConfiguration.getResolvedStatusName());
                            break;
                        case CLOSED:
                            issuesEntity.setStatus(redmineConfiguration.getClosedStatusName());
                            break;
                        default:
                            throw new RuntimeException("Action Not Allowed");
                    }
                    issuesEntity.setResolvedBy(ClientInfo.getClientId());
                    issueRepository.save(issuesEntity);
                }
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            log.error("exception while updating issue for id: {}, ex: {}", issuesEntity.getIssueId(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<UploadResponse> uploadFile(String filename, byte[] fileContent, ClientTypeEnum clientType) {
        String key = getClientApiKey(clientType);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/octet-stream");
            headers.set("X-Redmine-API-Key", key);

            HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileContent, headers);

            String uploadUrl = redmineConfiguration.getBaseUrl() + "/uploads.json?filename=" + filename;
            ResponseEntity<String> responseEntityString = restTemplate.exchange(uploadUrl, HttpMethod.POST, requestEntity, String.class);
            String responseBody = responseEntityString.getBody();
//            System.out.println("Response Body: " + responseBody);
            // Parse the response into UploadResponse
            ObjectMapper mapper = new ObjectMapper();
            UploadResponse uploadResponse = mapper.readValue(responseBody, UploadResponse.class);

            return ResponseEntity.ok(uploadResponse);
        } catch (HttpClientErrorException e) {
            log.error("exception while uploading file: {}", e);
            if (e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                String errorMessage = "This file cannot be uploaded because it exceeds the maximum allowed file size (5 MB)";
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(UploadResponse.builder()
                        .upload(UploadResponse.UploadData.builder().message(errorMessage).build()).build());
            } else {
                return ResponseEntity.status(e.getStatusCode()).body(null);
            }
        } catch (Exception e) {
            log.error("exception while uploading file: {}", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    public ResponseEntity<RedmineResponse> getRedmineIssueWithJournals(String issueId, ClientTypeEnum clientType) {
        String key = getClientApiKey(clientType);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("X-Redmine-API-Key", key);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(redmineConfiguration.getBaseUrl() + "/issues/" + issueId + ".json?include=journals,attachments", HttpMethod.GET, requestEntity, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            RedmineResponse issue = objectMapper.readValue(responseEntity.getBody(), RedmineResponse.class);
            return new ResponseEntity<>(issue, responseEntity.getStatusCode());
        } catch (Exception e) {
            log.error("exception while fetching issue for id: {}, ex: {}",issueId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public DashboardResponse getDashboardResponse(){
        return null;
    }

    public String getClientApiKey(ClientTypeEnum type) {
        switch (type) {
            case END_USER:
                return redmineConfiguration.getEndUserApiKey();
            case REGISTERED:
//                Optional<String> email = genericRepository.getEmailFromUsername(ClientInfo.getClientId());
//                if(!email.isPresent()) {
//                    throw new RuntimeException("No email id found for the user "+ClientInfo.getClientId());
//                }
                Optional<String> key = redmineGenericRepository.getApiKey(ClientInfo.getClientId());
                return key.orElseThrow(() -> new RuntimeException("User not found"));
            default:
                throw new IllegalArgumentException("Unknown client type: " + type);
        }
    }

    public ResponseEntity<List<CategoryResponse>> getIssueCategories(Integer projectIdOrIdentifier, ClientTypeEnum clientType) {
        String key = getClientApiKey(clientType);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("X-Redmine-API-Key", key);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            String baseUrl = redmineConfiguration.getBaseUrl();

            ResponseEntity<String> responseEntity = restTemplate.exchange(baseUrl + "/projects/" + projectIdOrIdentifier + "/issue_categories.json", HttpMethod.GET, requestEntity, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            IssueCategoriesResponse categoriesResponse = objectMapper.readValue(responseEntity.getBody(), IssueCategoriesResponse.class);

            // Extract the names of all categories
            List<CategoryResponse> categoryResponses = categoriesResponse.getIssue_categories().stream()
                    .map(issueCategory -> new CategoryResponse(issueCategory.getId(), issueCategory.getName()))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(categoryResponses, responseEntity.getStatusCode());
        } catch (Exception e) {
            log.error("Exception while fetching issue categories for project: {}, ex: {}", projectIdOrIdentifier, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

