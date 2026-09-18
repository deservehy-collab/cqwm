package com.sky.service;

import com.sky.vo.*;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ReportService {
    //营业额统计
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);
    //用户统计
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);
    //订单统计
    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);
    //销售统计
    SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end);
    //导出运营数据报表
    void exportBusinessData(HttpServletResponse response);
}
