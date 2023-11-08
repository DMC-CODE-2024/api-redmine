package com.gl.ceir.supportmodule.repository;

import com.gl.ceir.supportmodule.model.Issue;
import com.gl.ceir.supportmodule.model.IssuesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueRepository extends JpaRepository<IssuesEntity,Long>, JpaSpecificationExecutor<IssuesEntity> {
//    String columns = "select id,user_type,user_id,raised_by,resolved_by,ticket_id,mobile_number,email,category,created_at,updated_at,redmine_issue_id,first_name,last_name,subject,status,feedback,rating,is_private";
//    @Query(value = columns+ " from issues where ticket_id = :id")
    public Optional<IssuesEntity> findByTicketId(String ticketId);
    public List<IssuesEntity> findByMsisdn(String msisdn);
}
