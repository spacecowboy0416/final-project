package com.finalproject.coordi.closet.mapper;

import com.finalproject.coordi.closet.dto.ClosetItemDto;
import com.finalproject.coordi.closet.dto.CoordiItemDto;
import com.finalproject.coordi.closet.dto.SavedCoordiDto;
import com.finalproject.coordi.recommendation.dto.persistent.ProductDto;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClosetMapper {

    // 저장된 전체 코디 목록 조회
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

    // 특정 코디에 속한 상세 아이템 정보 조회
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

    // 코디 세트 기본 정보 저장
    @Insert("INSERT INTO recommendation " +
            "(user_id, weather_id, input_mode, input_text, product_option, tpo_type, style_type, is_saved, ai_blueprint, ai_explanation) " +
            "VALUES (#{userId}, #{weatherId}, #{inputMode}, #{inputText}, #{productOption}, #{tpoType}, #{styleType}, true, CAST(#{aiBlueprint} AS JSON), #{aiExplanation})")
    @Options(useGeneratedKeys = true, keyProperty = "recId")
    void insertSavedCoordi(SavedCoordiDto dto);

    // 코디 세트 구성품 매핑 저장
    @Insert("INSERT INTO recommendation_item (rec_id, slot_key, source_type, closet_item_id, product_id) " +
            "VALUES (#{recId}, #{slotKey}, #{sourceType}, #{closetItemId}, #{productId})")
    void insertRecommendationItem(@Param("recId") Long recId, @Param("slotKey") String slotKey, @Param("sourceType") String sourceType, @Param("closetItemId") Long closetItemId, @Param("productId") Long productId);

    // 저장된 코디 정보 수정
    @Update("UPDATE recommendation " +
            "SET input_text = #{inputText}, product_option = #{productOption}, tpo_type = #{tpoType}, " +
            "style_type = #{styleType}, ai_explanation = #{aiExplanation}, ai_blueprint = CAST(#{aiBlueprint} AS JSON) " +
            "WHERE rec_id = #{recId} AND user_id = #{userId}")
    void updateSavedCoordi(SavedCoordiDto dto);

    // 개별 옷장 아이템 목록 조회
    @Select("SELECT c.item_id as itemId, c.user_id as userId, c.product_id as productId, " +
            "p.category_id as categoryId, cat.name as categoryName, p.name, p.brand, p.color, p.material, p.fit, p.style, " +
            "p.season, p.image_url as imageUrl, c.is_active as isActive, c.created_at as createdAt " +
            "FROM closet_item c " +
            "LEFT JOIN product p ON c.product_id = p.product_id " +
            "LEFT JOIN category cat ON p.category_id = cat.category_id " +
            "WHERE c.user_id = #{userId} AND c.is_active = true " +
            "ORDER BY c.created_at DESC")
    List<ClosetItemDto> findItemsByUserId(Long userId);

    // 개별 상품 정보 저장
    @Insert("INSERT INTO product (source, category_id, name, brand, image_url, color, material, fit, style, season) " +
            "VALUES ('USER_CUSTOM', #{categoryId}, #{name}, #{brand}, #{imageUrl}, #{color}, #{material}, #{fit}, #{style}, #{season})")
    @Options(useGeneratedKeys = true, keyProperty = "productId")
    void insertUserProduct(ProductDto product);

    // 옷장 아이템 연결 저장
    @Insert("INSERT INTO closet_item (user_id, product_id, is_active) VALUES (#{userId}, #{productId}, true)")
    @Options(useGeneratedKeys = true, keyProperty = "itemId")
    void insertClosetItem(ClosetItemDto closetItemDto);

    // 1. 코디 세트의 입력 모드 확인 (AI 코디인지 사용자가 만든 수동 세트인지 구분용)
    @Select("SELECT input_mode FROM recommendation WHERE rec_id = #{recId}")
    String findInputModeByRecId(@Param("recId") Long recId);

    // 2. 특정 아이템의 Product ID 찾기
    @Select("SELECT product_id FROM closet_item WHERE item_id = #{itemId} AND user_id = #{userId}")
    Long findProductIdByItemId(@Param("itemId") Long itemId, @Param("userId") Long userId);

    // 3. 코디 세트에 묶인 옷장 아이템 ID 목록 찾기
    @Select("SELECT closet_item_id FROM recommendation_item WHERE rec_id = #{recId}")
    List<Long> findClosetItemIdsByRecId(@Param("recId") Long recId);

    // 4. [삭제] 아이템과 코디 연결 고리 삭제
    @Delete("DELETE FROM recommendation_item WHERE closet_item_id = #{itemId}")
    void deleteRecItemsByClosetItemId(@Param("itemId") Long itemId);

    // 5. [삭제] 코디 세트 안의 모든 옷 연결 고리 일괄 삭제
    @Delete("DELETE FROM recommendation_item WHERE rec_id = #{recId}")
    void deleteRecItemsByRecId(@Param("recId") Long recId);

    // 6. [삭제] 옷장 아이템 삭제
    @Delete("DELETE FROM closet_item WHERE item_id = #{itemId}")
    void deleteClosetItemById(@Param("itemId") Long itemId);

    // 7. [삭제] 유저가 올린 실제 사진(상품) 정보 삭제
    @Delete("DELETE FROM product WHERE product_id = #{productId} AND source = 'USER_CUSTOM'")
    void deleteUserCustomProduct(@Param("productId") Long productId);

    // 8. [삭제] 코디 세트 데이터 완전 삭제
    @Delete("DELETE FROM recommendation WHERE rec_id = #{recId} AND user_id = #{userId}")
    void deleteRecommendationById(@Param("recId") Long recId, @Param("userId") Long userId);
}