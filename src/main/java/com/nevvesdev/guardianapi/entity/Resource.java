package com.nevvesdev.guardianapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String ownerId;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
}