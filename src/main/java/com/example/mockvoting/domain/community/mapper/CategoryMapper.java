package com.example.mockvoting.domain.community.mapper;

import com.example.mockvoting.domain.community.dto.CategoryResponseDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {
    // 게시글 카테고리 전체 조회
    List<CategoryResponseDTO> selectAllCategories();

    // 활성화 게시판 조회
    List<CategoryResponseDTO> selectCategoriesByIsActive();

    // 활성화된 게시판에 속하는 게시글 전체 개수 조회 (+ 검색 조건)
    Integer selectPostCountFromActiveCategories(@Param("searchType") String searchType,
                                                @Param("keyword") String keyword);

    // 카테고리별 게시글 개수 조회 (+ 검색 조건)
    Integer selectPostCountByCategoryWithSearch(@Param("categoryCode") String categoryCode,
                                            @Param("searchType") String searchType,
                                            @Param("keyword") String keyword);

    // 카테고리별 게시글 개수 조회
    int selectPostCountByCategory(@Param("categoryCode") String categoryCode);

    // code로 익명 게시판 여부 조회
    boolean selectIsAnonymousByCode(@Param("code") String code);

    // id로 익명 게시판 여부 조회
    boolean selectIsAnonymousById(@Param("id") Long id);

    void updateCategoryStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);

    void insertCategory(CategoryResponseDTO categoryDTO);

    // 기존 메서드 아래 추가
    void deleteCategory(@Param("id") Long id);

}
