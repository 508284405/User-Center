package com.yuwang.usercenter.domain.menu.repository;

import com.yuwang.usercenter.domain.menu.entity.Menu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapper<Menu> {
    @Select("SELECT * FROM menu_permissions WHERE system_id = #{systemId}")
    List<Menu> findBySystemId(Long systemId);

    @Select("SELECT * FROM menu_permissions WHERE parent_id = #{parentId}")
    List<Menu> findByParentId(Long parentId);

    @Select("SELECT * FROM menu_permissions WHERE system_id = #{systemId} AND parent_id = #{parentId} ORDER BY sort")
    List<Menu> findBySystemIdAndParentIdOrderBySort(Long systemId, Long parentId);

    @Select("SELECT COUNT(*) > 0 FROM menu_permissions WHERE menu_name = #{menuName} AND system_id = #{systemId}")
    boolean existsByMenuNameAndSystemId(String menuName, Long systemId);
}