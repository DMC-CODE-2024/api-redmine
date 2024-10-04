package com.gl.ceir.supportmodule.model.app;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "duplicate_device_detail")
public class DuplicateDeviceDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "modified_on")
    private LocalDateTime modifiedOn;

    @Column(name = "imei")
    private String imei;

    @Column(name = "imsi")
    private String imsi;

    @Column(name = "msisdn")
    private String msisdn;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "edr_time")
    private LocalDateTime edrTime;

    @Column(name = "operator")
    private String operator;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "remark")
    private String remark;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "document_type1")
    private String documentType1;

    @Column(name = "document_type2")
    private String documentType2;

    @Column(name = "document_type3")
    private String documentType3;

    @Column(name = "document_type4")
    private String documentType4;

    @Column(name = "document_file_name_1")
    private String documentFileName1;

    @Column(name = "document_file_name_2")
    private String documentFileName2;

    @Column(name = "document_file_name_3")
    private String documentFileName3;

    @Column(name = "document_file_name_4")
    private String documentFileName4;

    @Column(name = "reminder_status")
    private Integer reminderStatus;

    @Column(name = "success_count")
    private Integer successCount;

    @Column(name = "fail_count")
    private Integer failCount;

    @Column(name = "approve_transaction_id")
    private String approveTransactionId;

    @Column(name = "approve_remark")
    private String approveRemark;

    @Column(name = "document_path1")
    private String documentPath1;

    @Column(name = "document_path2")
    private String documentPath2;

    @Column(name = "document_path3")
    private String documentPath3;

    @Column(name = "document_path4")
    private String documentPath4;

    @Column(name = "actual_imei")
    private String actualImei;

    @Column(name = "status")
    private String status;

    @Column(name = "redmine_tkt_id")
    private String redmineTktId;
}

