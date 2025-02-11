package com.yuwang.usercenter.domain.system.repository;

import com.yuwang.usercenter.domain.system.entity.System;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface SystemMapper extends BaseMapper<System> {
    @Select("SELECT * FROM systems WHERE system_code = #{systemCode} LIMIT 1")
    Optional<System> findBySystemCode(String systemCode);

    @Select("SELECT COUNT(*) > 0 FROM systems WHERE system_code = #{systemCode}")
    boolean existsBySystemCode(String systemCode);
}