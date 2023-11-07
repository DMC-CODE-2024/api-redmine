package com.gl.ceir.supportmodule.model;


import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Entity
@Data
@Builder
@Table(name = "issues")
public class IssuesEntity extends BaseEntity{
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
        AtomicLong counter = new AtomicLong(0);
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DATE_FORMATTER);
        long uniqueNumber = counter.incrementAndGet() % 100_000_000;
        String numberPart = String.format("%08d", uniqueNumber);
        ticketId = PREFIX + datePart + numberPart;
    }

}
