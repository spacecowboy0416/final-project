package com.finalproject.coordi.users.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.finalproject.coordi.users.dto.UserDto;
import java.util.Optional;

@Mapper
public interface UserMapperInter {
    Optional<UserDto> findByProviderAndProviderUserId(@Param("provider") String provider, @Param("providerUserId") String providerUserId);
    Optional<UserDto> findByEmail(String email);
    void save(UserDto user);
    void update(UserDto user);
}
