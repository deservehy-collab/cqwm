package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    //新增购物车数据
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //根据用户id还有菜品id/套餐id/口味查询购物车数据
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //判断是否存在
        if (list.isEmpty()) {
            //新增购物车数据
            //判断是菜品还是套餐
            if (shoppingCartDTO.getSetmealId() == null) {
                //获取菜品id
                Long dishId = shoppingCartDTO.getDishId();
                //新增菜品
                Dish dish = dishMapper.getById(dishId);
                //设置菜品id
                shoppingCart.setDishId(dishId);
                //设置菜品名称
                shoppingCart.setName(dish.getName());
                //设置菜品图片
                shoppingCart.setImage(dish.getImage());
                //设置菜品金额
                shoppingCart.setAmount(dish.getPrice());
                //设置数量为1
                shoppingCart.setNumber(1);
                //设置创建时间
                shoppingCart.setCreateTime(LocalDateTime.now());
            } else {
                //获取套餐id
                Long setmealId = shoppingCartDTO.getSetmealId();
                //新增套餐
                Setmeal setmeal = setmealMapper.getById(setmealId);
                //设置套餐id
                shoppingCart.setSetmealId(setmealId);
                //设置套餐名称
                shoppingCart.setName(setmeal.getName());
                //设置套餐图片
                shoppingCart.setImage(setmeal.getImage());
                //设置套餐金额
                shoppingCart.setAmount(setmeal.getPrice());
                //设置数量为1
                shoppingCart.setNumber(1);
                //设置创建时间
                shoppingCart.setCreateTime(LocalDateTime.now());
            }
            //新增购物车数据
            shoppingCartMapper.insert(shoppingCart);
        } else {
            //更新购物车数据
            ShoppingCart cart = list.get(0);
            //更新数量
            cart.setNumber(cart.getNumber() + 1);
            //更新购物车数量
            shoppingCartMapper.updateNumberById(cart);
        }
    }

    //查看购物车列表
    @Override
    public List<ShoppingCart> list() {
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }
    //清空购物车
    @Override
    public void clean() {
        //根据用户id删除购物车数据
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
    }
    //减少购物车数量
    @Override
    public void sub(ShoppingCartDTO shoppingDTO) {
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();
        //获取购物车数据
        ShoppingCart cart = shoppingCartMapper.getByUserId(userId);
               //查看传进来的是套餐还是菜品
        if (shoppingDTO.getSetmealId() == null) {
            //菜品
            //判断数量是否大于1
            if (cart.getNumber() == 1) {
                //删除购物车数据
                shoppingCartMapper.deleteByUserId(userId);
                return;
            }else if (cart.getNumber() > 1) {
                //更新数量
                cart.setNumber(cart.getNumber() - 1);
                //更新购物车数量
                shoppingCartMapper.updateNumberById(cart);
            }
        } else {
            //套餐
            //判断数量是否大于1
            if (cart.getNumber() == 1) {
                //删除购物车数据
                shoppingCartMapper.deleteByUserId(userId);
                return;
            }else if (cart.getNumber() > 1) {
                //更新数量
                cart.setNumber(cart.getNumber() - 1);
                //更新购物车数量
                shoppingCartMapper.updateNumberById(cart);
            }
        }
    }

}