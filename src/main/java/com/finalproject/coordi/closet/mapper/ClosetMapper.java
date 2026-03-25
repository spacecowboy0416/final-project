package com.finalproject.coordi.closet.mapper;

import com.finalproject.coordi.closet.dto.ClosetItemDto;
import com.finalproject.coordi.closet.dto.CoordiItemDto;
import com.finalproject.coordi.closet.dto.SavedCoordiDto;
import com.finalproject.coordi.recommendation.dto.persistent.ProductDto;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClosetMapper {

    // [회원 관리] 유저 닉네임 조회
    @Select("SELECT nickname FROM users WHERE user_id = #{userId}")
    String findNicknameByUserId(@Param("userId") Long userId);

    // [회원 관리] 유저 프로필 이미지 URL 조회
    @Select("SELECT profile_image_url FROM users WHERE user_id = #{userId}")
    String findProfileImageUrlByUserId(@Param("userId") Long userId);

    // [회원 관리] 유저 정보 수정 (닉네임, 프로필 사진)
    @Update("UPDATE users SET nickname = #{nickname}, profile_image_url = #{profileImageUrl} WHERE user_id = #{userId}")
    void updateUserProfile(@Param("userId") Long userId, @Param("nickname") String nickname, @Param("profileImageUrl") String profileImageUrl);

    // [탈퇴 관리] 유저 관련 코디 아이템 일괄 삭제
    @Delete("DELETE ri FROM recommendation_item ri JOIN recommendation r ON ri.rec_id = r.rec_id WHERE r.user_id = #{userId}")
    void deleteAllRecItemsByUserId(@Param("userId") Long userId);

    // [탈퇴 관리] 유저 관련 코디 추천 내역 일괄 삭제
    @Delete("DELETE FROM recommendation WHERE user_id = #{userId}")
    void deleteAllRecommendationsByUserId(@Param("userId") Long userId);

    // [탈퇴 관리] 유저가 등록한 커스텀 상품 ID 목록 조회 (고아 데이터 방지용)
    @Select("SELECT p.product_id FROM product p JOIN closet_item c ON p.product_id = c.product_id WHERE c.user_id = #{userId} AND p.source = 'USER_CUSTOM'")
    List<Long> findCustomProductIdsByUserId(@Param("userId") Long userId);

    // [탈퇴 관리] 유저 옷장 아이템 일괄 삭제
    @Delete("DELETE FROM closet_item WHERE user_id = #{userId}")
    void deleteAllClosetItemsByUserId(@Param("userId") Long userId);

    // [탈퇴 관리] 커스텀 상품 마스터 데이터 최종 삭제
    @Delete("<script>" +
            "DELETE FROM product WHERE product_id IN " +
            "<foreach item='id' collection='productIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    void deleteProductsByIds(@Param("productIds") List<Long> productIds);

    // [회원 관리] 유저 정보 최종 물리 삭제
    @Delete("DELETE FROM users WHERE user_id = #{userId}")
    void deleteUserById(@Param("userId") Long userId);

    // [저장 관리] 전체 코디 목록 조회
    @Select("SELECT rec_id as recId, user_id as userId, weather_id as weatherId, " +
            "input_mode as inputMode, input_text as inputText, product_option as productOption, " +
            "tpo_type as tpoType, style_type as styleType, " +
            "is_saved as isSaved, ai_blueprint as aiBlueprint, ai_explanation as aiExplanation, " +
            "created_at as createdAt " +
            "FROM recommendation " +
            "WHERE user_id = #{userId} AND is_saved = true " +
            "ORDER BY created_at DESC")
    @Results({
        @Result(property = "recId", column = "recId", id = true),
        @Result(property = "coordiItems", column = "recId", many = @Many(select = "findItemsByRecId"))
    })
    List<SavedCoordiDto> findSavedCoordis(Long userId);

    // [저장 관리] 특정 코디 상세 구성품 조회
    @Select("SELECT ri.rec_item_id as recItemId, ri.slot_key as slotKey, ri.source_type as sourceType, " +
            "ri.closet_item_id as closetItemId, ri.product_id as productId, " +
            "COALESCE(p1.image_url, p2.image_url) as imageUrl, " +
            "COALESCE(p1.name, p2.name) as name " +
            "FROM recommendation_item ri " +
            "LEFT JOIN product p1 ON ri.product_id = p1.product_id " +
            "LEFT JOIN closet_item ci ON ri.closet_item_id = ci.item_id " +
            "LEFT JOIN product p2 ON ci.product_id = p2.product_id " +
            "WHERE ri.rec_id = #{recId}")
    List<CoordiItemDto> findItemsByRecId(Long recId);

    // [코디 관리] 코디 기본 정보 저장
    @Insert("INSERT INTO recommendation " +
            "(user_id, weather_id, input_mode, input_text, product_option, tpo_type, style_type, is_saved, ai_blueprint, ai_explanation) " +
            "VALUES (#{userId}, #{weatherId}, #{inputMode}, #{inputText}, #{productOption}, #{tpoType}, #{styleType}, true, CAST(#{aiBlueprint} AS JSON), #{aiExplanation})")
    @Options(useGeneratedKeys = true, keyProperty = "recId")
    void insertSavedCoordi(SavedCoordiDto dto);

    // [코디 관리] 코디 저장 정보 수정
    @Update("UPDATE recommendation " +
            "SET input_text = #{inputText}, product_option = #{productOption}, tpo_type = #{tpoType}, " +
            "style_type = #{styleType}, ai_explanation = #{aiExplanation}, ai_blueprint = CAST(#{aiBlueprint} AS JSON) " +
            "WHERE rec_id = #{recId} AND user_id = #{userId}")
    void updateSavedCoordi(SavedCoordiDto dto);

    // [코디 관리] 제목 단독 수정
    @Update("UPDATE recommendation SET input_text = #{newTitle} WHERE rec_id = #{recId} AND user_id = #{userId}")
    void updateSavedCoordiTitle(@Param("recId") Long recId, @Param("newTitle") String newTitle, @Param("userId") Long userId);

    // [옷장 관리] 개별 아이템 리스트 조회
    @Select("SELECT c.item_id as itemId, c.user_id as userId, c.product_id as productId, " +
            "p.category_id as categoryId, cat.name as categoryName, p.name, p.brand, p.color, p.material, p.fit, p.style, " +
            "p.season, p.image_url as imageUrl, c.is_active as isActive, c.created_at as createdAt " +
            "FROM closet_item c " +
            "LEFT JOIN product p ON c.product_id = p.product_id " +
            "LEFT JOIN category cat ON p.category_id = cat.category_id " +
            "WHERE c.user_id = #{userId} AND c.is_active = true " +
            "ORDER BY c.created_at DESC")
    List<ClosetItemDto> findItemsByUserId(Long userId);

    // [상품 관리] 개별 상품 정보 저장
    @Insert("INSERT INTO product (source, category_id, name, brand, image_url, color, material, fit, style, season) " +
            "VALUES ('USER_CUSTOM', #{categoryId}, #{name}, #{brand}, #{imageUrl}, #{color}, #{material}, #{fit}, #{style}, #{season})")
    @Options(useGeneratedKeys = true, keyProperty = "productId")
    void insertUserProduct(ProductDto product);

    // [상품 관리] 상품 정보 및 이미지 URL 수정 (이미지 변경 핵심 쿼리)
    @Update("UPDATE product SET name = #{name}, brand = #{brand}, color = #{color}, season = #{season}, image_url = #{imageUrl} " +
            "WHERE product_id = #{productId}")
    void updateUserProduct(ProductDto product);

    // [옷장 관리] 옷장 아이템 연결 저장
    @Insert("INSERT INTO closet_item (user_id, product_id, is_active) VALUES (#{userId}, #{productId}, true)")
    @Options(useGeneratedKeys = true, keyProperty = "itemId")
    void insertClosetItem(ClosetItemDto closetItemDto);

    // [조회 관리] 코디 추천 입력 모드 조회
    @Select("SELECT input_mode FROM recommendation WHERE rec_id = #{recId}")
    String findInputModeByRecId(@Param("recId") Long recId);

    // [조회 관리] 특정 아이템의 상품 식별자 조회
    @Select("SELECT product_id FROM closet_item WHERE item_id = #{itemId} AND user_id = #{userId}")
    Long findProductIdByItemId(@Param("itemId") Long itemId, @Param("userId") Long userId);

    // [조회 관리] 코디 포함 아이템 리스트 조회
    @Select("SELECT closet_item_id FROM recommendation_item WHERE rec_id = #{recId}")
    List<Long> findClosetItemIdsByRecId(@Param("recId") Long recId);

    // [삭제 관리] 구성품 매핑 정보 삭제
    @Delete("DELETE FROM recommendation_item WHERE closet_item_id = #{itemId}")
    void deleteRecItemsByClosetItemId(@Param("itemId") Long itemId);

    @Delete("DELETE FROM recommendation_item WHERE rec_id = #{recId}")
    void deleteRecItemsByRecId(@Param("recId") Long recId);

    // [삭제 관리] 옷장 매핑 데이터 삭제
    @Delete("DELETE FROM closet_item WHERE item_id = #{itemId}")
    void deleteClosetItemById(@Param("itemId") Long itemId);

    // [삭제 관리] 사용자 커스텀 상품 데이터 삭제
    @Delete("DELETE FROM product WHERE product_id = #{productId} AND source = 'USER_CUSTOM'")
    void deleteUserCustomProduct(@Param("productId") Long productId);

    // [삭제 관리] 코디 추천 정보 삭제
    @Delete("DELETE FROM recommendation WHERE rec_id = #{recId} AND user_id = #{userId}")
    void deleteRecommendationById(@Param("recId") Long recId, @Param("userId") Long userId);
    
    // [옷장 관리] 코디 세트 구성품 매핑 저장 기능
    @Insert("INSERT INTO recommendation_item (rec_id, slot_key, source_type, closet_item_id, product_id) " +
            "VALUES (#{recId}, #{slotKey}, #{sourceType}, #{closetItemId}, #{productId})")
    void insertRecommendationItem(@Param("recId") Long recId, @Param("slotKey") String slotKey, @Param("sourceType") String sourceType, @Param("closetItemId") Long closetItemId, @Param("productId") Long productId);
}