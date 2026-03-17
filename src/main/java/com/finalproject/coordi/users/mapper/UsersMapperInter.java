package com.finalproject.coordi.users.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.finalproject.coordi.users.dto.UsersDto;

@Mapper
public interface UsersMapperInter {
    public UsersDto findByEmail(String email);
    public UsersDto findById(Long userId);
    public UsersDto findByProviderAndProviderUserId(@Param("provider") String provider, @Param("providerUserId") String providerUserId);
    public void save(UsersDto user);
    public void update(UsersDto user);
}
