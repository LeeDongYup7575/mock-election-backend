package com.example.mockvoting.domain.youtube.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "youtube_cache")
public class YoutubeCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String query;

    @Lob
    private String response;

    private LocalDateTime cachedAt;
}

