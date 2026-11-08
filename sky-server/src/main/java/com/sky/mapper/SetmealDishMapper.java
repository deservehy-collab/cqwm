package com.sky.mapper;

import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    //批量插入套餐菜品关系数据
    void insertBatch(List<SetmealDish> setmealDishes);

    //根据菜品id列表查询套餐id列表
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    //根据套餐id删除套餐菜品关系数据
    void deleteBySetmealId(Long id);

    //根据套餐id查询套餐菜品关系数据
    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getBySetmealId(Long id);
}