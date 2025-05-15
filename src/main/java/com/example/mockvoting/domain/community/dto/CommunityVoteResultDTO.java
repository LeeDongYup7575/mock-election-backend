package com.example.mockvoting.domain.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityVoteResultDTO {
    private Long targetId;
    private Byte vote;
}
