package com.gl.ceir.supportmodule.model;


import com.gl.ceir.supportmodule.Constants.ClientTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "issues")
public class IssuesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_type")
    private String userType;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "raised_by")
    private String raisedBy;
    @Column(name = "resolved_by")
    private String resolvedBy;
    @Column(name = "ticket_id", length = 36)
    private String ticketId;
    @Column(name = "mobile_number")
    private String msisdn;
    @Column(name = "email")
    private String email;
    @Column(name = "category")
    private String category;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createAt = LocalDateTime.now();
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    @Column(name = "redmine_issue_id")
    private int issueId;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "subject")
    private String subject;
    @Column(name = "status")
    private String status;
    @Column(name = "feedback")
    private String feedback;
    @Column(name = "rating")
    private String rating;
    @Column(name = "is_private")
    private Boolean isPrivate;

    @PrePersist
    public void onCreate() {
        String PREFIX = "ST";
        DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DATE_FORMATTER);
        Random random = new Random();
        int randomValue = random.nextInt(100_000_000);
        String numberPart = String.format("%08d", randomValue);
        ticketId = PREFIX + datePart + numberPart;
    }

}
