package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    //新增菜品和口味数据
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        //新增菜品数据
        //new Dish
        Dish dish = new Dish();
        //转换为菜品实体类
        BeanUtils.copyProperties(dishDTO, dish);
        //新增菜品数据一条
        dishMapper.insert(dish);
        //获取菜品id
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            //设置菜品id
            flavors.forEach(item -> item.setDishId(dishId));
            //新增菜品口味数据多条
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    //分页查询菜品
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        //分页查询菜品
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        //查询菜品数据
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        //转换为分页结果
        PageResult pageResult = new PageResult();
        pageResult.setTotal(page.getTotal());
        pageResult.setRecords(page.getResult());
        return pageResult;
    }

    //批量删除菜品
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        //判断当前菜品是否能够删除--起售中的菜品不能删除
        for (Long id:ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                //起售中的菜品不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //判断当前菜品是否能够删除--被套餐关联的菜品不能删除
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            //被套餐关联的菜品不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品数据
        for (Long id:ids) {
            dishMapper.deleteById(id);
            //删除菜品口味数据
            dishFlavorMapper.deleteByDishId(id);
        }

    }
    //根据id查询菜品
    @Override
    public DishVO getById(Long id) {
        Dish dish = dishMapper.getById(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        return dishVO;
    }
    //根据id查询菜品和口味数据
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //根据id查询菜品数据
        Dish dish = dishMapper.getById(id);
        //根据菜品id查询菜品口味数据
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        //转换为菜品vo
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        //设置菜品口味数据
        dishVO.setFlavors(flavors);
        return dishVO;
    }
    //修改菜品
    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //修改菜品数据
        dishMapper.updateById(dish);
        //删除原有菜品口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //重新插入菜品口味数据多条
        List<DishFlavor> flavors = dishDTO.getFlavors();
    }

}