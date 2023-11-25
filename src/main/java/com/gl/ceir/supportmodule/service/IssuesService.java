package com.gl.ceir.supportmodule.service;

import com.gl.ceir.supportmodule.model.app.IssuesEntity;
import com.gl.ceir.supportmodule.repository.app.IssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class IssuesService {

    @Value("${pagination-page-limit}")
    private Integer limit;

    @Autowired
    private IssueRepository issuesRepository;

    public Page<IssuesEntity> getFilteredIssues(
            String startDate, String endDate, String ticketId, String contactNumber,
            String status, String clientType, Integer page, Integer size
    ) {
        RequestValidator.validatePagination(page, size, limit);
        Specification<IssuesEntity> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (startDate != null && endDate != null) {
                RequestValidator.validateTimes(startDate, endDate);
                LocalDateTime startDateTime = LocalDateTime.of(LocalDate.parse(startDate), LocalTime.MIDNIGHT);
                LocalDateTime endDateTime = LocalDateTime.of(LocalDate.parse(endDate), LocalTime.MAX);
                predicates.add(criteriaBuilder.between(root.get("createAt"), startDateTime, endDateTime));
            }

            if (ticketId != null) {
                predicates.add(criteriaBuilder.equal(root.get("ticketId"), ticketId));
            }

            if (contactNumber != null) {
                predicates.add(criteriaBuilder.equal(root.get("msisdn"), contactNumber));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (clientType != null) {
                predicates.add(criteriaBuilder.equal(root.get("userType"), clientType));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        try {
            // Apply pagination
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createAt"));

            // Fetch filtered issues
            Page<IssuesEntity> pageResult = issuesRepository.findAll(specification, pageRequest);

            return pageResult;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

