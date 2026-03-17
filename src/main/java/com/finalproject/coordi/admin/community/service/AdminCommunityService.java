package com.finalproject.coordi.admin.community.service;

import com.finalproject.coordi.admin.community.dto.AdminCommunityDto;
import com.finalproject.coordi.admin.community.mapper.CommunityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCommunityService {

    private final CommunityMapper communityMapper;

    public List<AdminCommunityDto> getPostList() {
        return communityMapper.selectBoardPostList();
    }

    @Transactional
    public void modifyPostVisibility(Long postId, boolean isPublic) {
        communityMapper.updatePostVisibility(postId, isPublic);
    }

    @Transactional
    public void removePost(Long postId) {
        communityMapper.deletePost(postId);
    }
}
