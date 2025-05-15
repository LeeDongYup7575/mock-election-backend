package com.example.mockvoting.domain.report.entity;

import jakarta.persistence.*;

@Entity
@Table()
public class ReportType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30, unique = true)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 300)
    private String description;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
