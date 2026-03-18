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

    // 관리자에게 전체 게시글 현황을 제공하기 위해 DB에서 모든 게시글을 조회함.
    public List<AdminCommunityDto> getPostList() {
        return communityMapper.selectBoardPostList();
    }

    // 관리자가 게시글의 노출 여부를 제어할 수 있도록, 특정 게시글의 is_public 플래그를 업데이트함.
    @Transactional
    public void modifyPostVisibility(Long postId, boolean isPublic) {
        communityMapper.updatePostVisibility(postId, isPublic);
    }

    // 관리자가 불필요한 게시글을 삭제할 수 있도록, DB에서 특정 게시글 레코드를 제거함.
    @Transactional
    public void removePost(Long postId) {
        communityMapper.deletePost(postId);
    }

}
