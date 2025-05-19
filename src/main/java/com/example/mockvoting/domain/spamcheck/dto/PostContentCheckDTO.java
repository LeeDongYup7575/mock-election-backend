package com.example.mockvoting.domain.spamcheck.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostContentCheckDTO {
    private String title;
    private String content;
}
