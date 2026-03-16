package com.finalproject.coordi.closet.mapper;

import com.finalproject.coordi.closet.dto.ClosetItemDto;
import com.finalproject.coordi.closet.dto.SavedCoordiDto;
import com.finalproject.coordi.recommendation.dto.persistent.ProductDto;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClosetMapper {

    // 코디 추천 결과
    @Select("SELECT rec_id as recId, user_id as userId, weather_id as weatherId, " +
            "input_mode as inputMode, input_text as inputText, product_option as productOption, " +
            "tpo_type as tpoType, style_type as styleType, " +
            "is_saved as isSaved, ai_blueprint as aiBlueprint, ai_explanation as aiExplanation, " +
            "created_at as createdAt " +
            "FROM recommendation " +
            "WHERE user_id = #{userId} AND is_saved = true " +
            "ORDER BY created_at DESC")
    List<SavedCoordiDto> findSavedCoordis(Long userId);

    @Insert("INSERT INTO recommendation " +
            "(user_id, weather_id, input_mode, input_text, product_option, tpo_type, style_type, is_saved, ai_blueprint, ai_explanation) " +
            "VALUES (#{userId}, #{weatherId}, #{inputMode}, #{inputText}, #{productOption}, #{tpoType}, #{styleType}, true, #{aiBlueprint}, #{aiExplanation})")
            "VALUES (#{userId}, #{weatherId}, #{inputMode}, #{inputText}, #{productOption}, #{tpoType}, #{styleType}, true, CAST(#{aiBlueprint} AS JSON), #{aiExplanation})")
    @Options(useGeneratedKeys = true, keyProperty = "recId")
    void insertSavedCoordi(SavedCoordiDto dto);

    @Update("UPDATE recommendation " +
            "SET input_text = #{inputText}, tpo_type = #{tpoType}, style_type = #{styleType}, " +
            "ai_explanation = #{aiExplanation}, ai_blueprint = #{aiBlueprint} " +
            "SET input_text = #{inputText}, product_option = #{productOption}, tpo_type = #{tpoType}, " +
            "style_type = #{styleType}, ai_explanation = #{aiExplanation}, ai_blueprint = CAST(#{aiBlueprint} AS JSON) " +
            "WHERE rec_id = #{recId} AND user_id = #{userId}")
    void updateSavedCoordi(SavedCoordiDto dto);

    @Update("UPDATE recommendation SET is_saved = false WHERE rec_id = #{recId} AND user_id = #{userId}")
    void unsaveCoordi(@Param("recId") Long recId, @Param("userId") Long userId);

    // 카테고리 테이블 조인하여 이름 가져오기
    @Select("SELECT c.item_id as itemId, c.user_id as userId, c.product_id as productId, " +
            "p.name, p.image_url as imageUrl, p.season, p.category_id as categoryId, " +
            "cat.name as categoryName " + 
            "FROM closet_item c " +
            "LEFT JOIN product p ON c.product_id = p.product_id " +
            "LEFT JOIN category cat ON p.category_id = cat.category_id " +
    @Select("SELECT c.item_id as itemId, c.user_id as userId, c.product_id as productId, " +
            "p.category_id as categoryId, p.name, p.brand, p.color, p.material, p.fit, p.style, " +
            "p.season, p.image_url as imageUrl, c.is_active as isActive, c.created_at as createdAt " +
            "FROM closet_item c " +
            "LEFT JOIN product p ON c.product_id = p.product_id " +
            "WHERE c.user_id = #{userId} AND c.is_active = true " +
            "ORDER BY c.created_at DESC")
    List<ClosetItemDto> findItemsByUserId(Long userId);

    // Product 테이블 필수값인 category_id 추가
    @Insert("INSERT INTO product (source, name, category_id, season, image_url) " +
            "VALUES ('USER_CUSTOM', #{name}, #{categoryId}, #{season}, #{imageUrl})")
    @Insert("INSERT INTO product (source, category_id, name, brand, image_url, color, material, fit, style, season) " +
            "VALUES ('USER_CUSTOM', #{categoryId}, #{name}, #{brand}, #{imageUrl}, #{color}, #{material}, #{fit}, #{style}, #{season})")
    @Options(useGeneratedKeys = true, keyProperty = "productId")
    void insertUserProduct(ProductDto product);

    // 생성된 Product를 내 옷장(closet_item)에 연결
    @Insert("INSERT INTO closet_item (user_id, product_id, is_active) " +
            "VALUES (#{userId}, #{productId}, true)")
    void insertClosetItem(@Param("userId") Long userId, @Param("productId") Long productId);

    @Update("UPDATE closet_item SET is_active = false WHERE item_id = #{itemId} AND user_id = #{userId}")
    void deleteItem(@Param("itemId") Long itemId, @Param("userId") Long userId);
}
