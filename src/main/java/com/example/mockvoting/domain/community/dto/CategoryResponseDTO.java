package com.example.mockvoting.domain.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Boolean isAnonymous;
    private int sortOrder;
    private Boolean isActive;
}
