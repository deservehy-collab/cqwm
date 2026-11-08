package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    //新增购物车
    @PostMapping("/add")
    public Result add(@RequestBody ShoppingCartDTO shoppingDTO) {
        log.info("新增购物车: {}", shoppingDTO);
        shoppingCartService.add(shoppingDTO);
        return Result.success();
    }

    //查看购物车列表
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list() {
        List<ShoppingCart> list = shoppingCartService.list();
        log.info("购物车列表: {}", list);
        return Result.success(list);
    }
    //清空购物车
    @DeleteMapping("/clean")
    public Result clean() {
        shoppingCartService.clean();
        return Result.success();
    }
    //减少购物车数量
    @PostMapping("/sub")
    public Result sub(@RequestBody ShoppingCartDTO shoppingDTO) {
        log.info("减少购物车数量: {}", shoppingDTO);
        shoppingCartService.sub(shoppingDTO);
        return Result.success();
    }
}