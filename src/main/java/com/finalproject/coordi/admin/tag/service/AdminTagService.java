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

    // 태그 관리 UI의 카테고리 필터링을 위해 DB에 정의된 모든 태그 '종류'를 조회함.
    public List<String> getTagTypes() {
        return tagMapper.selectTagTypes();
    }

    // 특정 카테고리에 해당하는 태그 목록을 관리자에게 보여주기 위해, 종류(type)별로 태그를 조회함.
    public List<TagDto> getTagsByType(String type) {
        return tagMapper.selectTagsByType(type);
    }

    // 새로운 태그를 DB에 안전하게 추가하기 위함이며, insert 후 auto-generated된 ID를 포함한 DTO를 반환함.
    @Transactional
    public TagDto addTag(TagDto tag) {
        try {
            tagMapper.insertTag(tag);
            return tag;
        } catch (Exception e) {
            // DB 제약 조건(Unique 등) 위반 시, 일반적인 Exception을 비즈니스 예외로 전환하여 처리함.
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    // 태그 이름을 수정하되, 존재하지 않는 태그에 대한 수정을 방지하기 위해 영향을 받은 행(row) 수를 확인함.
    @Transactional
    public void updateTag(Long tagId, String name) {
        // 업데이트된 행이 0개이면, 해당 ID의 리소스가 없다고 판단하고 예외를 발생시킴.
        if (tagMapper.updateTag(tagId, name) == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }

    // 태그를 삭제하되, 존재하지 않는 태그 삭제 시도를 감지하기 위해 영향을 받은 행(row) 수를 확인함.
    @Transactional
    public void deleteTag(Long tagId) {
        // 삭제된 행이 0개이면, 해당 ID의 리소스가 없다고 판단하고 예외를 발생시킴.
        if (tagMapper.deleteTag(tagId) == 0) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
    }
}
