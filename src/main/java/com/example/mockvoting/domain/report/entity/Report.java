package com.example.mockvoting.domain.report.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_type_id", nullable = false)
    private Long reportTypeId;

    @Column(name = "reporter_id", nullable = false, length = 50)
    private String reporterId;

    @Column(name = "reported_user_id", nullable = false, length = 50)
    private String reportedUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "reason", length = 300)
    private String reason;

    @Column(name = "reported_at", nullable = false, updatable = false)
    private LocalDateTime reportedAt;

    @Column(name = "is_confirmed")
    private Boolean confirmed;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @PrePersist
    protected void onCreate() {
        this.reportedAt = LocalDateTime.now();
    }

    public enum TargetType {
        POST,
        POST_COMMENT,
        FEED,
        FEED_COMMENT,
        CHAT
    }
}