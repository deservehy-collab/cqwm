package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.dto.StatisticsQueryDTO;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 根据时间段统计营业数据
     * @param begin
     * @param end
     * @return
     */
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        StatisticsQueryDTO statisticsQueryDTO = new StatisticsQueryDTO();
        statisticsQueryDTO.setBegin(begin);
        statisticsQueryDTO.setEnd(end);

        Integer totalOrderCount = orderMapper.countByMap(statisticsQueryDTO);

        statisticsQueryDTO.setStatus(Orders.COMPLETED);
        Double turnover = orderMapper.sumByMap(statisticsQueryDTO);
        turnover = turnover == null? 0.0 : turnover;

        Integer validOrderCount = orderMapper.countByMap(statisticsQueryDTO);

        Double unitPrice = 0.0;

        Double orderCompletionRate = 0.0;
        if(totalOrderCount != 0 && validOrderCount != 0){
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
            unitPrice = turnover / validOrderCount;
        }

        Integer newUsers = userMapper.countByMap(statisticsQueryDTO);

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }


    /**
     * 查询订单管理数据
     *
     * @return
     */
    public OrderOverViewVO getOrderOverView() {
        StatisticsQueryDTO statisticsQueryDTO = new StatisticsQueryDTO();
        statisticsQueryDTO.setBegin(LocalDateTime.now().with(LocalTime.MIN));
        statisticsQueryDTO.setStatus(Orders.TO_BE_CONFIRMED);

        Integer waitingOrders = orderMapper.countByMap(statisticsQueryDTO);

        statisticsQueryDTO.setStatus(Orders.CONFIRMED);
        Integer deliveredOrders = orderMapper.countByMap(statisticsQueryDTO);

        statisticsQueryDTO.setStatus(Orders.COMPLETED);
        Integer completedOrders = orderMapper.countByMap(statisticsQueryDTO);

        statisticsQueryDTO.setStatus(Orders.CANCELLED);
        Integer cancelledOrders = orderMapper.countByMap(statisticsQueryDTO);

        statisticsQueryDTO.setStatus(null);
        Integer allOrders = orderMapper.countByMap(statisticsQueryDTO);

        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

    /**
     * 查询菜品总览
     *
     * @return
     */
    public DishOverViewVO getDishOverView() {
        StatisticsQueryDTO statisticsQueryDTO = new StatisticsQueryDTO();
        statisticsQueryDTO.setStatus(StatusConstant.ENABLE);
        Integer sold = dishMapper.countByMap(statisticsQueryDTO);

        statisticsQueryDTO.setStatus(StatusConstant.DISABLE);
        Integer discontinued = dishMapper.countByMap(statisticsQueryDTO);

        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询套餐总览
     *
     * @return
     */
    public SetmealOverViewVO getSetmealOverView() {
        StatisticsQueryDTO statisticsQueryDTO = new StatisticsQueryDTO();
        statisticsQueryDTO.setStatus(StatusConstant.ENABLE);
        Integer sold = setmealMapper.countByMap(statisticsQueryDTO);

        statisticsQueryDTO.setStatus(StatusConstant.DISABLE);
        Integer discontinued = setmealMapper.countByMap(statisticsQueryDTO);

        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}