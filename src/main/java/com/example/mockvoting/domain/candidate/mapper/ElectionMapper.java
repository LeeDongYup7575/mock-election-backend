package com.example.mockvoting.domain.candidate.mapper;

import com.example.mockvoting.domain.candidate.entity.Election;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ElectionMapper {
    List<Election> findAllElections();
}
