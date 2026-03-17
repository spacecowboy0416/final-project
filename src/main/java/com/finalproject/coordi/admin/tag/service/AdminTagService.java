package com.finalproject.coordi.admin.tag.service;

import com.finalproject.coordi.admin.tag.mapper.TagMapper;
import com.finalproject.coordi.admin.tag.dto.TagDto;
import com.finalproject.coordi.exception.BusinessException;
import com.finalproject.coordi.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminTagService {

    private final TagMapper tagMapper;

    public List<String> getTagTypes() {
        return tagMapper.selectTagTypes();
    }

    public List<TagDto> getTagsByType(String type) {
        return tagMapper.selectTagsByType(type);
    }

    @Transactional
    public TagDto addTag(TagDto tag) {
        try {
            tagMapper.insertTag(tag);
            return tag;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    @Transactional
    public void updateTag(Long tagId, String name) {
        if (tagMapper.updateTag(tagId, name) == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }

    @Transactional
    public void deleteTag(Long tagId) {
        if (tagMapper.deleteTag(tagId) == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }
}
