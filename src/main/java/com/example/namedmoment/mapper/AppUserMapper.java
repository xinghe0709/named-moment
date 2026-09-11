package com.example.namedmoment.mapper;

import com.example.namedmoment.entity.AppUser;
import org.apache.ibatis.annotations.Param;

public interface AppUserMapper {

    AppUser selectByUsername(@Param("username") String username);

    AppUser selectById(@Param("id") Long id);

    int insert(AppUser user);
}
