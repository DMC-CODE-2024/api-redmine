package com.gl.ceir.supportmodule.repository.app;

import com.gl.ceir.supportmodule.model.app.IssuesEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueRepository extends JpaRepository<IssuesEntity, Long>, JpaSpecificationExecutor<IssuesEntity> {
    Optional<IssuesEntity> findByTicketId(String ticketId);
    Page<IssuesEntity> findByMsisdn(String msisdn, Pageable pageable);
    Optional<IssuesEntity> findByIssueId(int issueId);
}

