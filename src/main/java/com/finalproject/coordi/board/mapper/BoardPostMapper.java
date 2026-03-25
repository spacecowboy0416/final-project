package com.finalproject.coordi.board.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.finalproject.coordi.board.vo.BoardPostRow;
import com.finalproject.coordi.board.vo.BoardRecommendationItemRow;

// XML과 연결되는 인터페이스
@Mapper
public interface BoardPostMapper {

    // 게시글 등록
    int insertBoardPost(BoardPostRow post);

    // 저장 코디 날씨 상태 조회
    String findWeatherStatusByRecId(@Param("recId") Long recId);

    // 게시글 상세 조회
    BoardPostRow findBoardPostDetailById(@Param("postId") Long postId);

    // 게시글 목록 조회
    List<BoardPostRow> findBoardPostList(
            @Param("weather") String weather,
            @Param("style") String style,
            @Param("tpo") String tpo,
            @Param("sort") String sort,
            @Param("offset") int offset,
            @Param("size") int size
    );

    // 게시글 총 개수
    int countBoardPostList(
            @Param("weather") String weather,
            @Param("style") String style,
            @Param("tpo") String tpo
    );

    // 게시글 목록 카드용 미리보기 아이템 조회
    List<BoardRecommendationItemRow> findPreviewItemsByRecId(@Param("recId") Long recId);

    // 코디 아이템 조회
    List<BoardRecommendationItemRow> findRecommendationItemsByPostId(@Param("postId") Long postId);

    // 작성자 조회
    Long findBoardPostAuthorId(@Param("postId") Long postId);

    // 존재 여부
    Integer existsActivePost(@Param("postId") Long postId);

    // 조회수 증가
    int increaseViewCount(@Param("postId") Long postId);

    // 수정
    int updateBoardPost(
            @Param("postId") Long postId,
            @Param("title") String title,
            @Param("content") String content
    );

    // 삭제
	int deleteBoardPost(@Param("postId") Long postId);

    // recommendation 검증
    Long findRecommendationOwnerId(@Param("recId") Long recId);
    Boolean findRecommendationSaved(@Param("recId") Long recId);
    Integer existsRecommendation(@Param("recId") Long recId);
}