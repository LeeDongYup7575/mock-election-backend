package com.example.mockvoting.domain.report.mapper.converter;

import com.example.mockvoting.domain.report.dto.ReportCreateRequestDTO;
import com.example.mockvoting.domain.report.entity.Report;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReportDtoMapper {
    Report toEntity(ReportCreateRequestDTO reportCreateRequestDTO);
}
