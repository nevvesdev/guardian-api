package com.nevvesdev.guardianapi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "resource_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String resourceId;

    @Column(nullable = false)
    private String changedBy;

    @Column(nullable = false)
    private String changeType;

    @Column(columnDefinition = "TEXT")
    private String previousValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    @Column(nullable = false)
    private Long resourceVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}