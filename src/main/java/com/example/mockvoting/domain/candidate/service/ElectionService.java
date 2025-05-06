package com.example.mockvoting.domain.candidate.service;

import com.example.mockvoting.domain.candidate.entity.Election;
import com.example.mockvoting.domain.candidate.mapper.ElectionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ElectionService {
    @Autowired
    private ElectionMapper electionMapper;

    public List<Election> findAllElections() {
        return electionMapper.findAllElections();
    }
}
