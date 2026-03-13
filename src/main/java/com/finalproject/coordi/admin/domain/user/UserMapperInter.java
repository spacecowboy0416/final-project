package com.finalproject.coordi.admin.domain.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapperInter {

    List<User> findAll(@Param("role") String role, @Param("searchTerm") String searchTerm, @Param("sort") String sort);

    Optional<User> findById(Long userId);

    int updateUserStatus(@Param("userId") Long userId, @Param("status") String status);

    int updateUser(@Param("userId") Long userId, @Param("role") String role, @Param("status") String status);
}
