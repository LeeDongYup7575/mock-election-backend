package com.example.mockvoting.domain.candidate.controller;

import com.example.mockvoting.domain.candidate.entity.Candidate;
import com.example.mockvoting.domain.candidate.entity.Policy;
import com.example.mockvoting.domain.candidate.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/candidate")
public class CandidateController {
    @Autowired
    private CandidateService candidateService;

    @GetMapping("/list")
    public ResponseEntity<List<Candidate>> getCandidatesByElection (@RequestParam String sgId) {
        List<Candidate> candidates = candidateService.getCandidatesBySgId(sgId);
        return ResponseEntity.ok(candidates);
    }
    @GetMapping("/detail")
    public ResponseEntity<List<Policy>> getCandidateDetail(@RequestParam String sgId, @RequestParam String partyName) {
        List<Policy> policy = candidateService.getPolicy(sgId,partyName);
        return ResponseEntity.ok(policy);
    }
}
