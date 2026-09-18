package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.StatisticsQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    Integer countByCategoryId(@Param("categoryId") Long categoryId);

    //新增菜品
    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);
    //分页查询菜品
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);
    //根据id查询菜品
    Dish getById(Long id);
    //根据id删除菜品
    void deleteById(Long id);
    //根据菜品id修改菜品数据
    @AutoFill(OperationType.UPDATE)
    void updateById(Dish dish);
    //根据分类id查询菜品
    List<DishVO> listByCategoryId(@Param("categoryId") Long categoryId);
    //根据条件查询菜品
    List<Dish> list(Dish dish);


    /**
     * 根据条件统计菜品数量
     * @param statisticsQueryDTO
     * @return
     */
    Integer countByMap(StatisticsQueryDTO statisticsQueryDTO);

}