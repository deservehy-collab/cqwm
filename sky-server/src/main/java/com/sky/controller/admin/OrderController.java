package com.sky.controller.admin;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    //订单搜索
    @GetMapping("/conditionSearch")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("ordersPageQueryDTO = {}", ordersPageQueryDTO);
        //分页查询
        PageResult pageResult = orderService.pageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
    //订单详情
    @GetMapping("/details/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        log.info("id = {}", id);
        OrderVO orderVO = orderService.getDetail(id);
        return Result.success(orderVO);
    }
    //接单
    @PutMapping("/confirm")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("ordersConfirmDTO = {}", ordersConfirmDTO);
        //调用服务层方法
        orderService.confirmOrder(ordersConfirmDTO);
        return Result.success();
    }
    //拒单
    @PutMapping("/rejection")
    public Result reject(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        log.info("ordersRejectionDTO = {}", ordersRejectionDTO);
        //调用服务层方法
        orderService.rejectOrder(ordersRejectionDTO);
        return Result.success();
    }
    //取消订单
    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        log.info("ordersCancelDTO = {}", ordersCancelDTO);
        //调用服务层方法
        orderService.cancel(ordersCancelDTO.getId());
        return Result.success();
    }
    //派送订单
    @PutMapping("/delivery/{id}")
    public Result delivery(@PathVariable Long id) {
        log.info("id = {}", id);
        //调用服务层方法
        orderService.deliveryOrder(id);
        return Result.success();
    }
    //完成订单
    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable Long id) {
        log.info("id = {}", id);
        //调用服务层方法
        orderService.completeOrder(id);
        return Result.success();
    }
    //各个状态的订单数量统计
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics() {
        //调用服务层方法
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }
}