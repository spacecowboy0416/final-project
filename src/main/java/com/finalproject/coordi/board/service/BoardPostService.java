package com.finalproject.coordi.board.service;

import java.util.ArrayList;
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
import com.finalproject.coordi.board.dto.response.BoardPostPreviewItemResponse;
import com.finalproject.coordi.board.dto.response.BoardRecommendationItemResponse;
import com.finalproject.coordi.board.dto.response.BoardSavedCoordiItemResponse;
import com.finalproject.coordi.board.dto.response.BoardSavedCoordiResponse;
import com.finalproject.coordi.board.mapper.BoardCommentMapper;
import com.finalproject.coordi.board.mapper.BoardPostMapper;
import com.finalproject.coordi.board.vo.BoardCommentRow;
import com.finalproject.coordi.board.vo.BoardPostRow;
import com.finalproject.coordi.board.vo.BoardRecommendationItemRow;
import com.finalproject.coordi.closet.dto.CoordiItemDto;
import com.finalproject.coordi.closet.dto.SavedCoordiDto;
import com.finalproject.coordi.closet.service.ClosetService;
import com.finalproject.coordi.exception.board.BoardForbiddenException;
import com.finalproject.coordi.exception.board.BoardPostAlreadyDeletedException;
import com.finalproject.coordi.exception.board.BoardPostNotFoundException;
import com.finalproject.coordi.exception.board.RecommendationNotFoundException;
import com.finalproject.coordi.exception.board.RecommendationNotSavedException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardPostService {

    private static final int MAX_PREVIEW_ITEM_COUNT = 6;

    private final BoardPostMapper boardPostMapper;
    private final BoardCommentMapper boardCommentMapper;

    // closet 쪽 저장 코디를 게시판 작성 화면에서 재사용
    private final ClosetService closetService;

    // 게시글 작성
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
        post.setIsPublic(true); // 🔥 항상 공개

        boardPostMapper.insertBoardPost(post);

        return new BoardPostCreateResponse(post.getPostId());
    }

    // 게시글 작성 페이지에서 선택할 저장 코디 목록 조회
    @Transactional(readOnly = true)
    public List<BoardSavedCoordiResponse> getSavedCoordis(Long userId) {
        return closetService.getSavedCoordis(userId)
                .stream()
                .map(this::toBoardSavedCoordiResponse)
                .toList();
    }

    // 게시판 목록 조회
    @Transactional(readOnly = true)
    public BoardPostListResponse getPostList(String weather, String style, String tpo, String sort, int page, int size) {
        int offset = page * size;

        List<BoardPostListItemResponse> posts = boardPostMapper.findBoardPostList(weather, style, tpo, sort, offset, size)
                .stream()
                .map(this::toListItemResponse)
                .toList();

        int total = boardPostMapper.countBoardPostList(weather, style, tpo);
        boolean hasNext = total > offset + size;

        return new BoardPostListResponse(posts, page, size, hasNext);
    }

    // 게시글 상세 조회
    @Transactional
    public BoardPostDetailResponse getPostDetail(Long postId, Long loginUserId) {
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
                .map(commentRow -> toCommentResponse(commentRow, loginUserId))
                .toList();

        return toDetailResponse(row, items, comments, loginUserId);
    }

    // 게시글 수정
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
                request.content()
        );

        if (updated == 0) {
            throw new BoardPostNotFoundException();
        }
    }

    // 게시글 삭제
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

        boardCommentMapper.softDeleteCommentsByPostId(postId);

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

    // 저장 코디 -> 게시글 작성 화면용 DTO 변환
    private BoardSavedCoordiResponse toBoardSavedCoordiResponse(SavedCoordiDto coordi) {
        String weatherStatus = boardPostMapper.findWeatherStatusByRecId(coordi.getRecId());

        return new BoardSavedCoordiResponse(
                coordi.getRecId(),
                weatherStatus == null || weatherStatus.isBlank() ? "-" : weatherStatus,
                coordi.getStyleType(),
                coordi.getTpoType(),
                coordi.getAiExplanation(),
                toBoardSavedCoordiItems(coordi)
        );
    }

    // 저장 코디 내부 아이템을 화면용 리스트로 변환
    // 순서: 아우터 -> 상의 -> 하의 -> 신발 -> 악세사리(여러 개)
    private List<BoardSavedCoordiItemResponse> toBoardSavedCoordiItems(SavedCoordiDto coordi) {
        if (coordi.getCoordiItems() == null || coordi.getCoordiItems().isEmpty()) {
            return List.of();
        }

        List<CoordiItemDto> items = coordi.getCoordiItems();
        List<BoardSavedCoordiItemResponse> result = new ArrayList<>();

        // 1. 아우터 (1개)
        addFirstItem(result, items, "outerwear", "outer");

        // 2. 상의 (1개)
        addFirstItem(result, items, "tops", "top");

        // 3. 하의 (1개)
        addFirstItem(result, items, "bottoms", "bottom", "pants");

        // 4. 신발 (1개)
        addFirstItem(result, items, "shoes", "shoe");

        // 5. 악세사리 (여러 개 허용)
        addAllItems(result, items, "accessories", "accessory");

        return result;
    }

    private void addFirstItem(
            List<BoardSavedCoordiItemResponse> result,
            List<CoordiItemDto> items,
            String... targetSlots
    ) {
        for (CoordiItemDto item : items) {
            if (item == null) {
                continue;
            }
            if (!matchesAnySlot(item.getSlotKey(), targetSlots)) {
                continue;
            }
            if (!hasMeaningfulCoordiItem(item)) {
                continue;
            }

            result.add(toSavedCoordiItem(item));
            return;
        }
    }

    private void addAllItems(
            List<BoardSavedCoordiItemResponse> result,
            List<CoordiItemDto> items,
            String... targetSlots
    ) {
        for (CoordiItemDto item : items) {
            if (item == null) {
                continue;
            }
            if (!matchesAnySlot(item.getSlotKey(), targetSlots)) {
                continue;
            }
            if (!hasMeaningfulCoordiItem(item)) {
                continue;
            }

            result.add(toSavedCoordiItem(item));
        }
    }

    private boolean matchesAnySlot(String slotKey, String... targetSlots) {
        if (slotKey == null || targetSlots == null || targetSlots.length == 0) {
            return false;
        }

        String normalizedSlotKey = slotKey.toLowerCase();

        for (String targetSlot : targetSlots) {
            if (targetSlot != null && normalizedSlotKey.contains(targetSlot.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    private BoardSavedCoordiItemResponse toSavedCoordiItem(CoordiItemDto item) {
        return new BoardSavedCoordiItemResponse(
                item.getSlotKey(),
                toSlotLabel(item.getSlotKey()),
                item.getName() == null || item.getName().isBlank() ? "-" : item.getName(),
                item.getImageUrl() == null ? "" : item.getImageUrl()
        );
    }

    // 코디 아이템의 화면 노출 가능 여부 판단
    private boolean hasMeaningfulCoordiItem(CoordiItemDto item) {
        boolean hasName = item.getName() != null && !item.getName().isBlank();
        boolean hasImage = item.getImageUrl() != null && !item.getImageUrl().isBlank();
        return hasName || hasImage;
    }

    // slot_key를 한글 라벨로 변환
    private String toSlotLabel(String slotKey) {
        if (slotKey == null || slotKey.isBlank()) {
            return "기타";
        }

        return switch (slotKey.toLowerCase()) {
            case "outerwear", "outer" -> "아우터";
            case "tops", "top" -> "상의";
            case "bottoms", "bottom", "pants" -> "하의";
            case "shoes", "shoe" -> "신발";
            case "accessories", "accessory" -> "악세사리";
            default -> "기타";
        };
    }

    // 목록 VO -> Response DTO 변환
    private BoardPostListItemResponse toListItemResponse(BoardPostRow row) {
        String preview = row.getContent() == null
                ? ""
                : (row.getContent().length() > 80 ? row.getContent().substring(0, 80) + "..." : row.getContent());

        PreviewItemBundle previewBundle = buildPreviewItemBundle(row.getRecId());

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
                row.getWeatherStatus(),
                previewBundle.previewItems(),
                previewBundle.extraItemCount()
        );
    }

    // 목록 카드 미리보기 아이템 구성
    private PreviewItemBundle buildPreviewItemBundle(Long recId) {
        List<BoardRecommendationItemRow> allItems = boardPostMapper.findPreviewItemsByRecId(recId);

        List<BoardPostPreviewItemResponse> previewItems = new ArrayList<>();

        addFirstMatchedItem(previewItems, allItems, "tops");
        addFirstMatchedItem(previewItems, allItems, "bottoms");
        addFirstMatchedItem(previewItems, allItems, "shoes");

        addAllMatchedItems(previewItems, allItems, "outerwear");
        addAllMatchedItems(previewItems, allItems, "accessories");

        addRemainingItems(previewItems, allItems);

        int extraItemCount = Math.max(previewItems.size() - MAX_PREVIEW_ITEM_COUNT, 0);

        List<BoardPostPreviewItemResponse> visibleItems = previewItems.stream()
                .limit(MAX_PREVIEW_ITEM_COUNT)
                .toList();

        return new PreviewItemBundle(visibleItems, extraItemCount);
    }

    // 필수 슬롯은 첫 번째 아이템만 추가
    private void addFirstMatchedItem(
            List<BoardPostPreviewItemResponse> previewItems,
            List<BoardRecommendationItemRow> allItems,
            String targetSlotKey
    ) {
        if (previewItems.size() >= MAX_PREVIEW_ITEM_COUNT) {
            return;
        }

        for (BoardRecommendationItemRow item : allItems) {
            if (!isSameSlot(item.getSlotKey(), targetSlotKey)) {
                continue;
            }

            BoardPostPreviewItemResponse previewItem = toPreviewItemResponse(item);
            if (previewItem == null || containsSameImage(previewItems, previewItem.imageUrl())) {
                continue;
            }

            previewItems.add(previewItem);
            return;
        }
    }

    // 선택 슬롯은 가능한 만큼 추가
    private void addAllMatchedItems(
            List<BoardPostPreviewItemResponse> previewItems,
            List<BoardRecommendationItemRow> allItems,
            String targetSlotKey
    ) {
        for (BoardRecommendationItemRow item : allItems) {
            if (!isSameSlot(item.getSlotKey(), targetSlotKey)) {
                continue;
            }

            BoardPostPreviewItemResponse previewItem = toPreviewItemResponse(item);
            if (previewItem == null || containsSameImage(previewItems, previewItem.imageUrl())) {
                continue;
            }

            previewItems.add(previewItem);
        }
    }

    // 나머지 슬롯도 뒤에 추가
    private void addRemainingItems(
            List<BoardPostPreviewItemResponse> previewItems,
            List<BoardRecommendationItemRow> allItems
    ) {
        for (BoardRecommendationItemRow item : allItems) {
            BoardPostPreviewItemResponse previewItem = toPreviewItemResponse(item);

            if (previewItem == null || containsSameImage(previewItems, previewItem.imageUrl())) {
                continue;
            }

            boolean alreadyHandledSlot =
                    isSameSlot(item.getSlotKey(), "tops")
                            || isSameSlot(item.getSlotKey(), "bottoms")
                            || isSameSlot(item.getSlotKey(), "shoes")
                            || isSameSlot(item.getSlotKey(), "outerwear")
                            || isSameSlot(item.getSlotKey(), "accessories");

            if (alreadyHandledSlot) {
                continue;
            }

            previewItems.add(previewItem);
        }
    }

    // 목록 카드용 DTO 변환
    private BoardPostPreviewItemResponse toPreviewItemResponse(BoardRecommendationItemRow row) {
        if (row.getImageUrl() == null || row.getImageUrl().isBlank()) {
            return null;
        }

        return new BoardPostPreviewItemResponse(
                row.getSlotKey(),
                row.getProductName(),
                row.getImageUrl()
        );
    }

    // slot_key 비교
    private boolean isSameSlot(String slotKey, String targetSlotKey) {
        if (slotKey == null || targetSlotKey == null) {
            return false;
        }

        return slotKey.equalsIgnoreCase(targetSlotKey);
    }

    // 같은 이미지 중복 방지
    private boolean containsSameImage(List<BoardPostPreviewItemResponse> previewItems, String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return true;
        }

        return previewItems.stream()
                .anyMatch(item -> imageUrl.equals(item.imageUrl()));
    }

    // recommendation item VO -> Response DTO 변환
    private BoardRecommendationItemResponse toRecommendationItemResponse(BoardRecommendationItemRow row) {
        return new BoardRecommendationItemResponse(
                row.getRecItemId(),
                row.getSlotKey(),
                row.getPriority(),
                row.getProductName(),
                row.getBrand(),
                row.getImageUrl(),
                row.getLink(),
                row.getColor(),
                row.getMaterial(),
                row.getFit(),
                row.getStyle()
        );
    }

    // comment VO -> Response DTO 변환
    private BoardCommentResponse toCommentResponse(BoardCommentRow row, Long loginUserId) {
        boolean mine = row.getUserId() != null && row.getUserId().equals(loginUserId);

        return new BoardCommentResponse(
                row.getCommentId(),
                row.getPostId(),
                row.getUserId(),
                row.getNickname(),
                row.getContent(),
                row.getCreatedAt(),
                row.getUpdatedAt(),
                mine
        );
    }

    // 상세 응답 DTO 조립
    private BoardPostDetailResponse toDetailResponse(
            BoardPostRow row,
            List<BoardRecommendationItemResponse> items,
            List<BoardCommentResponse> comments,
            Long loginUserId
    ) {
        boolean mine = row.getUserId() != null && row.getUserId().equals(loginUserId);

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
                row.getWeatherStatus(),
                mine,
                items,
                comments
        );
    }

    // 목록 미리보기 아이템 묶음
    private record PreviewItemBundle(
            List<BoardPostPreviewItemResponse> previewItems,
            int extraItemCount
    ) {
    }
}