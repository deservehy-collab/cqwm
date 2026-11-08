package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    //新增菜品口味数据多条
    void insertBatch(List<DishFlavor> flavors);
    //根据菜品id删除菜品口味数据
    @Delete("delete from dish_flavor where dish_id = #{id}")
    void deleteByDishId(Long id);
    //根据菜品id查询菜品口味数据
    @Select("select * from dish_flavor where dish_id = #{id}")
    List<DishFlavor> getByDishId(Long id);
}