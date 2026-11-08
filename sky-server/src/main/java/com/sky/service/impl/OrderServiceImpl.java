package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private WebSocketServer websocketServer;



    //user
    //提交订单
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO orderSubmitDTO) {
        //处理各种业务异常（地址簿为空、购物车数据为空）
        //处理地址簿为空的情况
        AddressBook addressBook = addressBookMapper.getById(orderSubmitDTO.getAddressBookId());
        if(addressBook==null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //处理购物车数据为空的情况
        Long userId= BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if(list.isEmpty()){
            throw new AddressBookBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //向订单表插入1条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(orderSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddress(addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());
        orders.setUserId(userId);

        orderMapper.insert(orders);

        //向订单明细表插入n条数据
        ArrayList<OrderDetail> orderDetailList = new ArrayList<>();
        //遍历购物车数据
        for (ShoppingCart cart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetailList);

        //清空当前用户的购物车数据
        shoppingCartMapper.deleteByUserId(userId);
        //封装VO返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getByOpenid(userId.toString());

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
        //自己new一个
        JSONObject jsonObject =new JSONObject();

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        //通过websocket向客户端浏览器推送消息 type orderId content
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1);//1.订单支付成功 2.催单
        map.put("orderId", orders.getId());
        map.put("content", "订单号：" + outTradeNo + "支付成功");
        String json = JSONObject.toJSONString(map);
        //调用websocket方法发送消息
        websocketServer.sendToAllClient(json);


    }
    //查询历史订单
    @Override
    public PageResult historyOrders(int pageNum, int pageSize, Integer status) {
        //开启分页查询
        PageHelper.startPage(pageNum, pageSize);
        //封装dto分页查询对象
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setStatus(status);
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        //分页条件查询
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        //由接口可知需要封装为orderVO类型：订单菜品信息orderDishes，订单详情orderDetailList
        List<OrderVO> list = new ArrayList();
        // 查询出订单明细，并封装入OrderVO进行响应
        if (page != null && page.getTotal() > 0) { //有订单才有必要接着查询订单详情信息
            for (Orders orders : page) {
                Long orderId = orders.getId();// 订单id

                // 根据订单id,查询订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);

                //将订单明细拼接成字符串（如：鱼香肉丝×1、米饭×2）
                StringBuilder orderDishes = new StringBuilder();
                for (OrderDetail od : orderDetails) {
                    orderDishes.append(od.getName()).append("×").append(od.getNumber()).append("、");
                }
                String dishes = orderDishes.toString();
                if (dishes.length() > 0) {
                    dishes = dishes.substring(0, dishes.length() - 1);
                }
                orderVO.setOrderDishes(dishes);

                list.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), list);
    }
    //查询订单详情
    @Override
    public OrderVO orderDetail(Long id) {
        // 根据id查询订单
        Orders orders = orderMapper.getById(id);

        // 查询该订单对应的菜品/套餐明细
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // 将该订单及其详情封装到OrderVO并返回
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        //将订单明细拼接成字符串（如：鱼香肉丝×1、米饭×2）
        StringBuilder orderDishes = new StringBuilder();
        for (OrderDetail od : orderDetailList) {
            orderDishes.append(od.getName()).append("×").append(od.getNumber()).append("、");
        }
        String dishes = orderDishes.toString();
        if (dishes.length() > 0) {
            dishes = dishes.substring(0, dishes.length() - 1);
        }
        orderVO.setOrderDishes(dishes);

        return orderVO;
    }

    /**
     * 用户取消订单
     *
     * @param id
     */
    @Override
    public void cancel(Long id)  {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (ordersDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }


        //以上验证都通过后，此时订单处于待支付和待接单状态下
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

//        // 订单处于待接单状态下取消，需要进行退款
//        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
//            //调用微信支付退款接口
//            weChatPayUtil.refund(
//                    ordersDB.getNumber(), //商户订单号
//                    ordersDB.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额
//
//            //支付状态修改为 退款
//            orders.setPayStatus(Orders.REFUND);
//        }

        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }
    //再来一单
    @Override
    public void repetition(Long id) {
        //根据订单id查询订单明细（之前订单买了什么菜）
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        //遍历每一条订单明细，逐个添加到购物车
        for (OrderDetail orderDetail : orderDetailList) {
            //将订单明细里面的部分相同数据封装为shoppingcart的dto对象
            ShoppingCartDTO shoppingCartDTO = new ShoppingCartDTO();
            shoppingCartDTO.setDishId(orderDetail.getDishId());
            shoppingCartDTO.setSetmealId(orderDetail.getSetmealId());
            shoppingCartDTO.setDishFlavor(orderDetail.getDishFlavor());
            //将订单明细添加到购物车
            shoppingCartService.add(shoppingCartDTO);
        }





    }
    //催单
    @Override
    public void remind(Long id) {
        //根据订单id查询订单
        Orders ordersDB = orderMapper.getById(id);
        //校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //通过websocket向客户端浏览器推送消息 type orderId content
        Map<String, Object> map = new HashMap<>();
        map.put("type", 2);//1.订单支付成功 2.催单
        map.put("orderId", id);
        map.put("content", "订单号：" + id + "需要催单");
        String json = JSONObject.toJSONString(map);
        //调用websocket方法发送消息
        websocketServer.sendToAllClient(json);
    }


    //admin
    //分页条件查询订单
    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        //分页查询
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        //查询数据库
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }
    //查询订单详情
    @Override
    public OrderVO getDetail(Long id) {
        // 根据id查询订单
        Orders orders = orderMapper.getById(id);
        // 查询该订单对应的菜品/套餐明细
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // 将该订单及其详情封装到OrderVO并返回
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        //将订单明细拼接成字符串（如：鱼香肉丝×1、米饭×2）
        StringBuilder orderDishes = new StringBuilder();
        for (OrderDetail od : orderDetailList) {
            orderDishes.append(od.getName()).append("×").append(od.getNumber()).append("、");
        }
        String dishes = orderDishes.toString();
        if (dishes.length() > 0) {
            dishes = dishes.substring(0, dishes.length() - 1);
        }
        orderVO.setOrderDishes(dishes);

        return orderVO;
    }
    //接单
    @Override
    public void confirmOrder(OrdersConfirmDTO ordersConfirmDTO) {
        //根据订单id查询订单
        Orders orders = orderMapper.getById(ordersConfirmDTO.getId());
        //校验订单是否存在
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //校验订单状态是否为待接单
        if (!orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //更新订单状态为已接单
        orders.setStatus(Orders.CONFIRMED);
        //更新订单
        orderMapper.update(orders);
    }
    //拒单
    @Override
    public void rejectOrder(OrdersRejectionDTO ordersRejectionDTO) {
        //根据订单id查询订单
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());
        //校验订单是否存在
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //校验订单状态是否为待接单
        if (!orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //更新订单状态为已接单
        orders.setStatus(Orders.CANCELLED);
        //更新订单
        orderMapper.update(orders);
    }
    //派送订单
    @Override
    public void deliveryOrder(Long id) {
        //根据订单id查询订单
        Orders orders = orderMapper.getById(id);
        //校验订单是否存在
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //校验订单状态是否为待接单
        if (!orders.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //更新订单状态为派送中
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        //更新订单
        orderMapper.update(orders);
    }

    @Override
    public void completeOrder(Long id) {
        //根据订单id查询订单
        Orders orders = orderMapper.getById(id);
        //校验订单是否存在
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //校验订单状态是否为派送中
        if (!orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //更新订单状态为已完成
        orders.setStatus(Orders.COMPLETED);
        //更新订单
        orderMapper.update(orders);
    }
    //各个状态的订单数量统计
    @Override
    public OrderStatisticsVO statistics() {
        // 根据状态，分别查询出待接单、待派送、派送中的订单数量
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        // 将查询出的数据封装到orderStatisticsVO中响应
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }


}