package com.example.mockvoting.domain.candidate.controller;

import com.example.mockvoting.domain.candidate.entity.Election;
import com.example.mockvoting.domain.candidate.service.ElectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/election")
public class ElectionController {

    @Autowired
    private ElectionService electionService;

    @GetMapping("/list")
    public ResponseEntity<List<Election>> getElectionList(){
        List<Election> electionList = electionService.findAllElections();

        return ResponseEntity.ok(electionList);
    }

}
