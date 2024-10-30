package com.gl.ceir.supportmodule.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gl.ceir.supportmodule.dto.ClientInfo;
import com.gl.ceir.supportmodule.model.app.IssuesEntity;
import com.gl.ceir.supportmodule.repository.app.IssueRepository;
import com.gl.ceir.supportmodule.service.RequestValidator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.lang.invoke.SerializedLambda;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class IssuesService {
    private static final Logger log = LoggerFactory.getLogger(com.gl.ceir.supportmodule.service.IssuesService.class);

    @Value("${pagination-page-limit}")
    private Integer limit;

    @Value("${user-management-url}")
    private String userManagementServiceUrl;

    @Autowired
    private IssueRepository issuesRepository;

    private HttpClient httpClient = HttpClient.newHttpClient();

    private ObjectMapper objectMapper = new ObjectMapper();

    public Page<IssuesEntity> getFilteredIssues(String startDate, String endDate, String ticketId, String contactNumber, String status, String clientType, Integer page, Integer size, String raisedBy, boolean isMyDashboard) {
        RequestValidator.validatePagination(page, size, this.limit);
        Specification<IssuesEntity> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (startDate != null && endDate != null) {
                RequestValidator.validateTimes(startDate, endDate);
                LocalDateTime startDateTime = LocalDateTime.of(LocalDate.parse(startDate), LocalTime.MIDNIGHT);
                LocalDateTime endDateTime = LocalDateTime.of(LocalDate.parse(endDate), LocalTime.MAX);
                predicates.add(criteriaBuilder.between((Expression)root.get("createAt"), startDateTime, endDateTime));
            }
            if (ticketId != null) {
                String likePattern = "%" + ticketId + "%";
                predicates.add(criteriaBuilder.like((Expression)root.get("ticketId"), likePattern));
            }
            if (contactNumber != null)
                predicates.add(criteriaBuilder.equal((Expression)root.get("msisdn"), contactNumber));
            if (status != null)
                predicates.add(criteriaBuilder.equal((Expression)root.get("status"), status));
            if (clientType != null)
                predicates.add(criteriaBuilder.equal((Expression)root.get("userType"), clientType));
            List<String> emails = new ArrayList<>();
            if (isMyDashboard) {
                emails.add(ClientInfo.getLoggedInUser());
            } else {
                emails = fetchEmailsFromApi(ClientInfo.getLoggedInUser());
            }
            if (raisedBy != null) {
                if (emails != null && !emails.isEmpty()) {
                    if (emails.contains(raisedBy)) {
                        predicates.add(criteriaBuilder.equal((Expression)root.get("raisedBy"), raisedBy));
                    } else {
                        return criteriaBuilder.disjunction();
                    }
                } else {
                    return criteriaBuilder.disjunction();
                }
            } else {
                CriteriaBuilder.In<String> inClause = criteriaBuilder.in((Expression)root.get("raisedBy"));
                Objects.requireNonNull(inClause);
                emails.forEach(inClause::value);
                predicates.add(inClause);
            }
            return criteriaBuilder.and(predicates.<Predicate>toArray(new Predicate[0]));
        };
        try {
            PageRequest pageRequest = PageRequest.of(page.intValue(), size.intValue(), Sort.by(Sort.Direction.DESC, new String[] { "createAt" }));
            Page<IssuesEntity> pageResult = this.issuesRepository.findAll(specification, (Pageable)pageRequest);
            return pageResult;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<String> fetchEmailsFromApi(String clientId) {
        try {
            String url = String.format(userManagementServiceUrl, clientId);
            log.info("Fetching emails from user mgmt api: url=> "+url);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Optional<IssuesEntity> findByIssueId(int issueId) {
        return issuesRepository.findByIssueId(issueId);
    }

    public void updateIssueStatus(int issueId, String newStatus) {
        Optional<IssuesEntity> optionalIssue = findByIssueId(issueId);
        if (optionalIssue.isPresent()) {
            IssuesEntity issue = optionalIssue.get();
            issue.setStatus(newStatus);
            issuesRepository.save(issue);
        }
    }
}

