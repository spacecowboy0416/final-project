package com.finalproject.coordi.board.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finalproject.coordi.board.dto.request.BoardPostCreateRequest;
import com.finalproject.coordi.board.dto.request.BoardPostUpdateRequest;
import com.finalproject.coordi.board.dto.response.BoardCommentResponse;
import com.finalproject.coordi.board.dto.response.BoardPostCreateResponse;
import com.finalproject.coordi.board.dto.response.BoardPostDetailResponse;
import com.finalproject.coordi.board.dto.response.BoardPostListItemResponse;
import com.finalproject.coordi.board.dto.response.BoardPostListResponse;
import com.finalproject.coordi.board.dto.response.BoardRecommendationItemResponse;
import com.finalproject.coordi.board.mapper.BoardCommentMapper;
import com.finalproject.coordi.board.mapper.BoardPostMapper;
import com.finalproject.coordi.board.vo.BoardCommentRow;
import com.finalproject.coordi.board.vo.BoardPostRow;
import com.finalproject.coordi.board.vo.BoardRecommendationItemRow;
import com.finalproject.coordi.exception.board.BoardForbiddenException;
import com.finalproject.coordi.exception.board.BoardPostAlreadyDeletedException;
import com.finalproject.coordi.exception.board.BoardPostNotFoundException;
import com.finalproject.coordi.exception.board.RecommendationNotFoundException;
import com.finalproject.coordi.exception.board.RecommendationNotSavedException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardPostService {

    private final BoardPostMapper boardPostMapper;
    private final BoardCommentMapper boardCommentMapper;

    // 게시글 작성
    // 현재 로그인 사용자의 recommendation만 공유 가능하고, 저장된 코디만 허용한다.
    @Transactional
    public BoardPostCreateResponse createPost(Long loginUserId, BoardPostCreateRequest request) {
        validateRecommendationExists(request.recId());
        validateRecommendationOwnership(request.recId(), loginUserId);
        validateRecommendationSaved(request.recId());

        BoardPostRow post = new BoardPostRow();
        post.setUserId(loginUserId);
        post.setRecId(request.recId());
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setIsPublic(request.isPublic());

        boardPostMapper.insertBoardPost(post);

        return new BoardPostCreateResponse(post.getPostId());
    }

    // 게시판 목록 조회
    @Transactional(readOnly = true)
    public BoardPostListResponse getPostList(String weather, String style, String tpo, int page, int size) {
        int offset = page * size;

        List<BoardPostListItemResponse> posts = boardPostMapper.findBoardPostList(weather, style, tpo, offset, size)
                .stream()
                .map(this::toListItemResponse)
                .toList();

        int total = boardPostMapper.countBoardPostList(weather, style, tpo);
        boolean hasNext = total > offset + size;

        return new BoardPostListResponse(posts, page, size, hasNext);
    }

    // 게시글 상세 조회
    // 상세 조회 시 조회수를 1 증가시킨다.
    @Transactional
    public BoardPostDetailResponse getPostDetail(Long postId) {
        ensurePostExists(postId);

        boardPostMapper.increaseViewCount(postId);

        BoardPostRow row = boardPostMapper.findBoardPostDetailById(postId);
        if (row == null) {
            throw new BoardPostNotFoundException();
        }

        List<BoardRecommendationItemResponse> items = boardPostMapper.findRecommendationItemsByPostId(postId)
                .stream()
                .map(this::toRecommendationItemResponse)
                .toList();

        List<BoardCommentResponse> comments = boardCommentMapper.findCommentsByPostId(postId)
                .stream()
                .map(this::toCommentResponse)
                .toList();

        return toDetailResponse(row, items, comments);
    }

    // 게시글 수정
    // 작성자 본인만 수정 가능
    @Transactional
    public void updatePost(Long postId, Long loginUserId, BoardPostUpdateRequest request) {
        ensurePostExists(postId);

        Long authorId = boardPostMapper.findBoardPostAuthorId(postId);

        if (authorId == null) {
            throw new BoardPostNotFoundException();
        }
        if (!authorId.equals(loginUserId)) {
            throw new BoardForbiddenException();
        }

        int updated = boardPostMapper.updateBoardPost(
                postId,
                request.title(),
                request.content(),
                request.isPublic()
        );

        if (updated == 0) {
            throw new BoardPostNotFoundException();
        }
    }

    // 게시글 삭제
    // 작성자 본인만 삭제 가능
    @Transactional
    public void deletePost(Long postId, Long loginUserId) {
        ensurePostExists(postId);

        Long authorId = boardPostMapper.findBoardPostAuthorId(postId);

        if (authorId == null) {
            throw new BoardPostNotFoundException();
        }
        if (!authorId.equals(loginUserId)) {
            throw new BoardForbiddenException();
        }

        int deleted = boardPostMapper.softDeleteBoardPost(postId);
        if (deleted == 0) {
            throw new BoardPostAlreadyDeletedException();
        }
    }

    // recommendation 존재 여부 검증
    private void validateRecommendationExists(Long recId) {
        Integer exists = boardPostMapper.existsRecommendation(recId);
        if (exists == null || exists == 0) {
            throw new RecommendationNotFoundException();
        }
    }

    // recommendation 소유자 검증
    // 내 저장 코디만 게시글로 공유 가능해야 한다.
    private void validateRecommendationOwnership(Long recId, Long currentUserId) {
        Long ownerId = boardPostMapper.findRecommendationOwnerId(recId);
        if (ownerId == null) {
            throw new RecommendationNotFoundException();
        }
        if (!ownerId.equals(currentUserId)) {
            throw new BoardForbiddenException();
        }
    }

    // recommendation 저장 여부 검증
    private void validateRecommendationSaved(Long recId) {
        Boolean saved = boardPostMapper.findRecommendationSaved(recId);
        if (saved == null || !saved) {
            throw new RecommendationNotSavedException();
        }
    }

    // 삭제되지 않은 게시글 존재 여부 검증
    private void ensurePostExists(Long postId) {
        Integer exists = boardPostMapper.existsActivePost(postId);
        if (exists == null || exists == 0) {
            throw new BoardPostNotFoundException();
        }
    }

    // 목록 VO -> Response DTO 변환
    private BoardPostListItemResponse toListItemResponse(BoardPostRow row) {
        String preview = row.getContent() == null ? "" :
                (row.getContent().length() > 80 ? row.getContent().substring(0, 80) + "..." : row.getContent());

        return new BoardPostListItemResponse(
                row.getPostId(),
                row.getUserId(),
                row.getNickname(),
                row.getTitle(),
                preview,
                Boolean.TRUE.equals(row.getIsPublic()),
                row.getViewCount() == null ? 0 : row.getViewCount(),
                row.getCommentCount() == null ? 0 : row.getCommentCount(),
                row.getCreatedAt(),
                row.getRecId(),
                row.getStyleType(),
                row.getTpoType(),
                row.getWeatherId(),
                row.getWeatherStatus(),
                row.getTemp(),
                row.getPlaceName(),
                row.getTopItemName(),
                row.getBottomItemName(),
                row.getThumbnailImageUrl()
        );
    }

    // recommendation item VO -> Response DTO 변환
    private BoardRecommendationItemResponse toRecommendationItemResponse(BoardRecommendationItemRow row) {
        return new BoardRecommendationItemResponse(
                row.getRecItemId(),
                row.getSlotKey(),
                row.getPriority(),
                row.getProductName(),
                row.getBrand(),
                row.getPrice(),
                row.getImageUrl(),
                row.getLink(),
                row.getColor(),
                row.getMaterial(),
                row.getFit(),
                row.getStyle(),
                row.getSeason()
        );
    }

    // comment VO -> Response DTO 변환
    private BoardCommentResponse toCommentResponse(BoardCommentRow row) {
        return new BoardCommentResponse(
                row.getCommentId(),
                row.getPostId(),
                row.getUserId(),
                row.getNickname(),
                row.getContent(),
                row.getCreatedAt(),
                row.getUpdatedAt()
        );
    }

    // 상세 응답 DTO 조립
    private BoardPostDetailResponse toDetailResponse(
            BoardPostRow row,
            List<BoardRecommendationItemResponse> items,
            List<BoardCommentResponse> comments
    ) {
        return new BoardPostDetailResponse(
                row.getPostId(),
                row.getUserId(),
                row.getNickname(),
                row.getTitle(),
                row.getContent(),
                Boolean.TRUE.equals(row.getIsPublic()),
                row.getViewCount() == null ? 0 : row.getViewCount(),
                row.getCommentCount() == null ? 0 : row.getCommentCount(),
                row.getCreatedAt(),
                row.getUpdatedAt(),
                row.getRecId(),
                row.getStyleType(),
                row.getTpoType(),
                row.getAiExplanation(),
                row.getWeatherId(),
                row.getWeatherStatus(),
                row.getTemp(),
                row.getFeelsLike(),
                row.getHumidity(),
                row.getWindSpeed(),
                row.getPlaceName(),
                items,
                comments
        );
    }
}