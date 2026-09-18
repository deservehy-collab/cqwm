package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

//定时处理任务类
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    //处理超时订单,每1分钟执行一次,情况是订单状态为待付款
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder() {
        log.info("处理超时订单,当前时间:{}", LocalDateTime.now());
        //查询状态为付款中且下单时间<当前时间-15分钟的订单
        List<Orders> orders = orderMapper.getByStatusAndOrderLT(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(15));
        log.info("超时订单数量:{}", orders.size());
        //将订单状态改为已取消
        orders.forEach(order -> {
            order.setStatus(Orders.CANCELLED);
            order.setCancelTime(LocalDateTime.now());
            order.setCancelReason("超时未付款");
            orderMapper.update(order);
        });
    }

    //处理派送中订单,每天1点0分执行一次，当前时间减去一个小时为零点刷新,情况是收货后不点完成订单
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliverOrder() {
        log.info("处理超时订单,当前时间:{}", LocalDateTime.now());
        //查询状态为派送中
        List<Orders> orders = orderMapper.getByStatusAndOrderLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().minusHours(1));
        log.info("派送中订单数量:{}", orders.size());
        //将订单状态改为已完成
        orders.forEach(order -> {
            order.setStatus(Orders.COMPLETED);
            orderMapper.update(order);
        });

    }
}
