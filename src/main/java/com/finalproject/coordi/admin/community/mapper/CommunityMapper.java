package com.finalproject.coordi.admin.community.mapper;

import com.finalproject.coordi.admin.community.dto.AdminCommunityDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommunityMapper {

    List<AdminCommunityDto> selectBoardPostList();

    void updatePostVisibility(@Param("postId") Long postId, @Param("isPublic") boolean isPublic);

    void deletePost(@Param("postId") Long postId);

}
