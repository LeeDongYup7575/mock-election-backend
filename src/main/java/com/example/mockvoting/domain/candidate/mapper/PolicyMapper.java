package com.example.mockvoting.domain.candidate.mapper;

import java.util.List;

import com.example.mockvoting.domain.candidate.entity.Policy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PolicyMapper {
    List<Policy> findAllPolicy(@Param("sgId") String sgId, @Param("partyName") String partyName);
}
