package com.example.mockvoting.domain.candidate.service;

import com.example.mockvoting.domain.candidate.entity.Candidate;
import com.example.mockvoting.domain.candidate.entity.Policy;
import com.example.mockvoting.domain.candidate.mapper.CandidateMapper;
import com.example.mockvoting.domain.candidate.mapper.PolicyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandidateService {

    @Autowired
    private CandidateMapper candidatemapper;

    @Autowired
    private PolicyMapper policymapper;

    public List<Candidate> getCandidatesBySgId(String sgId) {
    return candidatemapper.findCandidatesBySgId(sgId);
    }

    public List<Policy> getPolicy(String sgId, String partyName){
        return policymapper.findAllPolicy(sgId,partyName);
    }
}


