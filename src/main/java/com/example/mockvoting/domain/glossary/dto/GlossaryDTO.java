package com.example.mockvoting.domain.glossary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlossaryDTO {
    private String term;
    private String definition;
    private String source;   // "DB" or "Wikipedia"
    private String pageUrl;  // 위키백과 링크 (optional)
}
