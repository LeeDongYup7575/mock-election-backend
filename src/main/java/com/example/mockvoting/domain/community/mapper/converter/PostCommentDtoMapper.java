package com.example.mockvoting.domain.community.mapper.converter;

import com.example.mockvoting.domain.community.dto.PostCommentCreateRequestDTO;
import com.example.mockvoting.domain.community.entity.PostComment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostCommentDtoMapper {
    PostComment toEntity(PostCommentCreateRequestDTO postCommentCreateRequestDTO);
}
