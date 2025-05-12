package com.example.mockvoting.domain.youtube.repository;

import com.example.mockvoting.domain.youtube.entity.YoutubeCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface YoutubeCacheRepository extends JpaRepository<YoutubeCache, Long> {
    Optional<YoutubeCache> findByQuery(String query);
}
