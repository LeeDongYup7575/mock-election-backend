package com.example.mockvoting.domain.glossary.repository;

import com.example.mockvoting.domain.glossary.entity.GlossaryTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GlossaryTermRepository extends JpaRepository<GlossaryTerm, Long> {
    List<GlossaryTerm> findByTermContainingIgnoreCase(String term);
}
