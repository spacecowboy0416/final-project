package com.finalproject.coordi.admin.tag.mapper;

import com.finalproject.coordi.admin.tag.dto.TagDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper {

    List<String> selectTagTypes();

    List<TagDto> selectTagsByType(@Param("type") String type);

    int insertTag(TagDto tag);

    int updateTag(@Param("tagId") Long tagId, @Param("name") String name);

    int deleteTag(Long tagId);

}
