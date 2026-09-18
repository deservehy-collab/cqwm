package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    //user
    //提交订单
    OrderSubmitVO submitOrder(OrdersSubmitDTO orderSubmitDTO);
    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);
    //查询历史订单
    PageResult historyOrders(int page, int pageSize, Integer status);
    //查询订单详情
    OrderVO orderDetail(Long id);
    //取消订单
    void cancel(Long id);
    //再来一单
    void repetition(Long id);
    //催单
    void remind(Long id);





    //admin
    //分页条件查询订单
    PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);
    //查询订单详情
    OrderVO getDetail(Long id);
    //接单
    void confirmOrder(OrdersConfirmDTO ordersConfirmDTO);
    //拒单
    void rejectOrder(OrdersRejectionDTO ordersRejectionDTO);
    //派送订单
    void deliveryOrder(Long id);
    //完成订单
    void completeOrder(Long id);
    //各个状态的订单数量统计
    OrderStatisticsVO statistics();

}