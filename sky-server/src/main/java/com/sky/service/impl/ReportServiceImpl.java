package com.sky.service.impl;

import com.sky.dto.StatisticsQueryDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WorkspaceService workspaceService;

    //营业额统计
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 从begin到end范围内的每天的日期
        // 当前集合用于存放从begin到end范围内的每天的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while (!begin.equals(end)){
            // 日期计算，计算指定日期的后一天对应的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 从数据库中查询从begin到end范围内的每天的营业额数据
        // 存放每天的营业额
        List<Double> turnoverList = new ArrayList<>();

        for (LocalDate date : dateList) {
            // 查询date日期对应的营业额数据，营业额是指：状态为“已完成”的订单金额合计
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // select sum(amount) from orders where order_time > beginTime and order_time < endTime and status = 5
            StatisticsQueryDTO statisticsQueryDTO = new StatisticsQueryDTO();
            statisticsQueryDTO.setBegin(beginTime);
            statisticsQueryDTO.setEnd(endTime);
            statisticsQueryDTO.setStatus(Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(statisticsQueryDTO);
            //判断营业额是否为null
            turnover=turnover==null?0.0:turnover;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(dateList.stream().map(LocalDate::toString).collect(Collectors.joining(",")))
                .turnoverList(turnoverList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .build();
    }

    //用户统计
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            
            //查询当天新增用户数
            StatisticsQueryDTO newUserQueryDTO = new StatisticsQueryDTO();
            newUserQueryDTO.setBegin(beginTime);
            newUserQueryDTO.setEnd(endTime);
            Integer newUserCount = userMapper.countByMap(newUserQueryDTO);
            newUserList.add(newUserCount == null ? 0 : newUserCount);
            
            //查询总用户数（截止到当天结束）
            StatisticsQueryDTO totalUserQueryDTO = new StatisticsQueryDTO();
            totalUserQueryDTO.setEnd(endTime);
            Integer totalUserCount = userMapper.countByMap(totalUserQueryDTO);
            totalUserList.add(totalUserCount == null ? 0 : totalUserCount);
        }
        
        return UserReportVO.builder()
                .dateList(dateList.stream().map(LocalDate::toString).collect(Collectors.joining(",")))
                .newUserList(newUserList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .totalUserList(totalUserList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .build();
    }
    //订单统计
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        // 从begin到end范围内的每天的日期
        // 当前集合用于存放从begin到end范围内的每天的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while (!begin.equals(end)) {
            // 日期计算，计算指定日期的后一天对应的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        // 从数据库中查询从begin到end范围内的每天的订单数据
        // 存放每天的订单数
        List<Integer> orderCountList = new ArrayList<>();
        // 存放每天的有效订单数
        List<Integer> validOrderCountList = new ArrayList<>();
        // 存放订单总数
        Integer totalOrderCount = 0;
        // 存放有效订单总数
        Integer validOrderCount = 0;

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //查询当天订单数
            StatisticsQueryDTO orderCountDTO = new StatisticsQueryDTO();
            orderCountDTO.setBegin(beginTime);
            orderCountDTO.setEnd(endTime);
            Integer orderCount = orderMapper.countByMap(orderCountDTO);
            orderCountList.add(orderCount == null ? 0 : orderCount);
            totalOrderCount += orderCount == null ? 0 : orderCount;

            //查询当天有效订单数
            StatisticsQueryDTO validOrderCountDTO = new StatisticsQueryDTO();
            validOrderCountDTO.setBegin(beginTime);
            validOrderCountDTO.setEnd(endTime);
            validOrderCountDTO.setStatus(Orders.COMPLETED);
            Integer validOrderCount1 = orderMapper.countByMap(validOrderCountDTO);
            validOrderCountList.add(validOrderCount1 == null ? 0 : validOrderCount1);
            validOrderCount += validOrderCount1 == null ? 0 : validOrderCount1;
        }
        //完成率等于有效订单数除以订单总数
        Number orderCompletionRate = validOrderCount == 0 ? 0.0 : validOrderCount / (double) totalOrderCount;

        //返回订单统计VO
        return OrderReportVO.builder()
                .dateList(dateList.stream().map(LocalDate::toString).collect(Collectors.joining(",")))
                .orderCountList(orderCountList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .validOrderCountList(validOrderCountList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate.doubleValue())
                .build();

    }
    //销售统计
    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        
        StatisticsQueryDTO statisticsQueryDTO = new StatisticsQueryDTO();
        statisticsQueryDTO.setBegin(beginTime);
        statisticsQueryDTO.setEnd(endTime);
        statisticsQueryDTO.setStatus(Orders.COMPLETED);
        
        List<Map<String, Object>> list = orderDetailMapper.getSalesTop10(statisticsQueryDTO);
        
        List<String> nameList = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();
        
        for (Map<String, Object> map : list) {
            nameList.add((String) map.get("name"));
            numberList.add(((Number) map.get("number")).intValue());
        }
        
        return SalesTop10ReportVO.builder()
                .nameList(String.join(",", nameList))
                .numberList(numberList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .build();
    }
    /**
     * 导出运营数据报表
     * @param response
     */
    public void exportBusinessData(HttpServletResponse response) {
        //1. 查询数据库，获取营业数据---查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));

        //2. 通过POI将数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            //基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //3. 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}