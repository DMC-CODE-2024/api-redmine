package com.gl.ceir.supportmodule.model.app;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
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

    @Column(name = "reminder_status")
    private Integer reminderStatus;

    @Column(name = "success_count")
    private Integer successCount;

    @Column(name = "fail_count")
    private Integer failCount;

    @Column(name = "actual_imei")
    private String actualImei;

    @Column(name = "status")
    private String status;

    @Column(name = "redmine_tkt_id")
    private String redmineTktId;
}

