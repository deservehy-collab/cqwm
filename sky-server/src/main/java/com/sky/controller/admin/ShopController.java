package com.sky.controller.admin;

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
public class ShopController {
    @Autowired
    private RedisConnectionFactory connectionFactory;

    public static final String KEY = "SHOP_STATUS";

    @PutMapping("/{status}")
//    @PostMapping("/{status}")
    public Result setStatus(@PathVariable Integer status) {
        log.info("========== [PUT] setStatus被调用, status={}, 含义={} ==========",
                status, status == 1 ? "营业中" : "打烊中");

        RedisConnection conn = null;
        try {
            conn = connectionFactory.getConnection();
            byte[] keyBytes = KEY.getBytes(StandardCharsets.UTF_8);
            byte[] valBytes = String.valueOf(status).getBytes(StandardCharsets.UTF_8);

            log.info("设置前, Redis中值为: {}", conn.get(keyBytes) != null
                    ? new String(conn.get(keyBytes), StandardCharsets.UTF_8) : "null");

            conn.set(keyBytes, valBytes);

            byte[] result = conn.get(keyBytes);
            String actualVal = new String(result, StandardCharsets.UTF_8);
            log.info("========== 写入验证: 期望={}, Redis实际值={} ==========", status, actualVal);
        } catch (Exception e) {
            log.error("Redis写入失败", e);
        } finally {
            if (conn != null) conn.close();
        }

        return Result.success();
    }

    @GetMapping("/status")
    public Result<Integer> getStatus() {
        RedisConnection conn = null;
        try {
            conn = connectionFactory.getConnection();
            byte[] result = conn.get(KEY.getBytes(StandardCharsets.UTF_8));
            Integer status = result != null ? Integer.valueOf(new String(result, StandardCharsets.UTF_8)) : null;
            log.info("========== 查询店铺状态: {} ==========", status);
            return Result.success(status);
        } finally {
            if (conn != null) conn.close();
        }
    }
}