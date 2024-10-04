package com.gl.ceir.supportmodule.repository.app;

import com.gl.ceir.supportmodule.model.app.DuplicateDeviceDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface DuplicateDeviceDetailRepository extends JpaRepository<DuplicateDeviceDetail, Long>, JpaSpecificationExecutor<DuplicateDeviceDetail> {
    Optional<DuplicateDeviceDetail> findByTransactionId(String transactionId);

    @Modifying
    @Transactional
    @Query("UPDATE DuplicateDeviceDetail d SET d.status = :status, d.updatedBy = :updatedBy WHERE d.redmineTktId = :redmineTktId")
    int updateStatusAndUpdatedByByRedmineTktId(@Param("status") String status, @Param("updatedBy") String updatedBy, @Param("redmineTktId") String redmineTktId);
}
