package com.sky.mapper;

import com.sky.dto.StatisticsQueryDTO;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderDetailMapper {
    //插入订单
    void insertBatch(List<OrderDetail> orderDetailList);
    //根据订单id查询订单详情
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);
    //查询销量排名前10
    List<Map<String, Object>> getSalesTop10(StatisticsQueryDTO statisticsQueryDTO);
}