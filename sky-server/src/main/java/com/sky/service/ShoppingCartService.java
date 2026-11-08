package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    //新增购物车数据
    void add(ShoppingCartDTO shoppingCartDTO);

    //查看购物车列表
    List<ShoppingCart> list();

    //清空购物车
    void clean();

    //减少购物车数量
    void sub(ShoppingCartDTO shoppingDTO);
}