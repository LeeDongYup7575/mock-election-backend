package com.example.mockvoting.domain.candidate.mapper;

import com.example.mockvoting.domain.candidate.entity.Candidate;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CandidateMapper {
    List<Candidate> findCandidatesBySgId(String sgId);
}
