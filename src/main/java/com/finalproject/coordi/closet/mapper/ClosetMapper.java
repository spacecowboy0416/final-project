package com.finalproject.coordi.closet.mapper;

import com.finalproject.coordi.closet.dto.ClosetItemDto;
import com.finalproject.coordi.closet.dto.SavedCoordiDto;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ClosetMapper {

    // 내 옷장에 저장된 코디 목록 조회
    @Select("SELECT rec_id as recId, user_id as userId, weather_id as weatherId, " +
            "input_mode as inputMode, input_text as inputText, product_option as productOption, " +
            "is_saved as isSaved, ai_blueprint as aiBlueprint, ai_explanation as aiExplanation, " +
            "created_at as createdAt " +
            "FROM recommendation " +
            "WHERE user_id = #{userId} AND is_saved = true " +
            "ORDER BY created_at DESC")
    List<SavedCoordiDto> findSavedCoordis(Long userId);

    // 코디추천 결과를 저장
    @Insert("INSERT INTO recommendation " +
            "(user_id, weather_id, input_mode, input_text, product_option, is_saved, ai_blueprint, ai_explanation) " +
            "VALUES (#{userId}, #{weatherId}, #{inputMode}, #{inputText}, #{productOption}, true, #{aiBlueprint}, #{aiExplanation})")
    @Options(useGeneratedKeys = true, keyProperty = "recId")
    void insertSavedCoordi(SavedCoordiDto dto);

    // 저장된 코디 정보 수정
    @Update("UPDATE recommendation " +
            "SET input_text = #{inputText}, ai_explanation = #{aiExplanation}, ai_blueprint = #{aiBlueprint} " +
            "WHERE rec_id = #{recId} AND user_id = #{userId}")
    void updateSavedCoordi(SavedCoordiDto dto);

    // 옷장에서 코디 제거 (논리적 삭제: is_saved 플래그를 false로 변경)
    @Update("UPDATE recommendation SET is_saved = false WHERE rec_id = #{recId} AND user_id = #{userId}")
    void unsaveCoordi(@Param("recId") Long recId, @Param("userId") Long userId);

    @Select("SELECT c.*, cat.name as categoryName " +
            "FROM closet_item c " +
            "LEFT JOIN category cat ON c.category_id = cat.category_id " +
            "WHERE c.user_id = #{userId} AND c.is_active = true " +
            "ORDER BY c.created_at DESC")
    List<ClosetItemDto> findItemsByUserId(Long userId);

    @Insert("INSERT INTO closet_item (user_id, category_id, name, color, season, thickness, image_url) " +
            "VALUES (#{userId}, #{categoryId}, #{name}, #{color}, #{season}, #{thickness}, #{imageUrl})")
    void insertItem(ClosetItemDto item);

    @Update("UPDATE closet_item SET is_active = false WHERE item_id = #{itemId} AND user_id = #{userId}")
    void deleteItem(@Param("itemId") Long itemId, @Param("userId") Long userId);
}