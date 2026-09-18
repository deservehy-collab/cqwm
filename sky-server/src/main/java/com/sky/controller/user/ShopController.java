package com.sky.controller.user;

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Slf4j
public class ShopController {
    @Autowired
    private RedisConnectionFactory connectionFactory;

    public static final String KEY = "SHOP_STATUS";

    @GetMapping("/status")
    public Result<Integer> getStatus() {
        RedisConnection conn = null;
        try {
            conn = connectionFactory.getConnection();
            byte[] result = conn.get(KEY.getBytes(StandardCharsets.UTF_8));
            Integer status = result != null ? Integer.valueOf(new String(result, StandardCharsets.UTF_8)) : null;
            log.info("========== 用户端查询店铺状态: {} ==========", status);
            return Result.success(status);
        } finally {
            if (conn != null) conn.close();
        }
    }
}