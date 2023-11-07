package com.gl.ceir.supportmodule.service;

import com.gl.ceir.supportmodule.model.IssuesEntity;
import com.gl.ceir.supportmodule.repository.IssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class IssuesService {

    @Value("${pagination-page-limit}")
    private Integer limit;

    @Autowired
    private IssueRepository issuesRepository;

    public List<IssuesEntity> getFilteredIssues(
            String startDate, String endDate, String ticketId, String contactNumber,
            String status, String clientType, Integer page, Integer size
    ) {
        RequestValidator.validate(startDate, endDate, page, size, limit);
        Specification<IssuesEntity> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (startDate != null && endDate != null) {
                predicates.add(criteriaBuilder.between(root.get("createAt"), LocalDateTime.parse(startDate), LocalDateTime.parse(endDate)));
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

            return pageResult.getContent();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}

