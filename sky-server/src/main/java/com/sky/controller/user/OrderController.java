package com.sky.controller.user;


import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    //提交订单
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO orderSubmitDTO) {
        log.info("提交订单，orderSubmitDTO={}", orderSubmitDTO);
        OrderSubmitVO orderSubmitVO=orderService.submitOrder(orderSubmitDTO);
        return Result.success(orderSubmitVO);
    }
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);

        //改成点击支付订单
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());

        return Result.success(orderPaymentVO);
    }

    //查询历史订单
    @GetMapping("/historyOrders")
    public Result<PageResult> historyOrders(int page,int pageSize,Integer status){
        PageResult pageResult=orderService.historyOrders(page,pageSize,status);
        return Result.success(pageResult);
       }

    //查询订单详情
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> orderDetail(@PathVariable Long id){
        return Result.success(orderService.orderDetail(id));
    }
    //取消订单
    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id){
        orderService.cancel(id);
        return Result.success();
    }
    //再来一单
    @PostMapping("/repetition/{id}")
    public Result<Void> again(@PathVariable Long id){
        orderService.repetition(id);
        return Result.success();
    }
    //催单
    @GetMapping("/reminder/{id}")
    public Result remind(@PathVariable Long id) {
        log.info("id = {}", id);
        //调用服务层方法
        orderService.remind(id);
        return Result.success();
    }
}