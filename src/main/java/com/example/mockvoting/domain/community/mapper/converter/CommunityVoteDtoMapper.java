package com.example.mockvoting.domain.community.mapper.converter;

import com.example.mockvoting.domain.community.dto.CommunityVoteRequestDTO;
import com.example.mockvoting.domain.community.entity.CommunityVote;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommunityVoteDtoMapper {
    CommunityVote toEntiy(CommunityVoteRequestDTO communityVoteRequestDTO);
}
