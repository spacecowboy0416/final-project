package com.finalproject.coordi.admin.mapper;

import com.finalproject.coordi.admin.dto.TagDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminMapper {

    List<String> selectTagTypes();

    List<TagDto> selectTagsByType(@Param("type") String type);

    int insertTag(TagDto tag);

    int updateTag(@Param("tagId") Long tagId, @Param("name") String name);

    int deleteTag(Long tagId);

}
