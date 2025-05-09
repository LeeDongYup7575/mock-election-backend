package com.example.mockvoting.domain.community.mapper.converter;

import com.example.mockvoting.domain.community.dto.PostCreateRequestDTO;
import com.example.mockvoting.domain.community.entity.Post;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostDtoMapper {

    Post toEntity(PostCreateRequestDTO postCreateRequestDTO);

}
