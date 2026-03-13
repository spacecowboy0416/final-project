package com.finalproject.coordi.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.finalproject.coordi.user.dto.UserDto;

@Mapper
public interface UserMapperInter {
    public UserDto findByEmail(String email);
    public UserDto findByUserId(Long userId);
    public UserDto findByProviderAndProviderUserId(@Param("provider") String provider, @Param("providerUserId") String providerUserId);
    public void save(UserDto user);
    public void update(UserDto user);
}
