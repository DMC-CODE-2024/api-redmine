package com.gl.ceir.supportmodule.repository;

import com.gl.ceir.supportmodule.model.Issue;
import com.gl.ceir.supportmodule.model.IssuesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueRepository extends JpaRepository<IssuesEntity,Long>, JpaSpecificationExecutor<IssuesEntity> {

    public Optional<IssuesEntity> findByTicketId(String id);
    public List<IssuesEntity> findByMsisdn(String msisdn);
}
