package com.sky.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 * 作用：自定义 RedisTemplate 的序列化方式，使其能够存储任意 Java 对象到 Redis
 */
@Configuration
@Slf4j
public class RedisConfiguration {

    /**
     * 创建并配置 RedisTemplate Bean
     *
     * RedisTemplate 是 Spring 操作 Redis 的核心工具类，相当于 JDBC 的 JdbcTemplate
     * 只有配置了正确的序列化器，才能将 Java 对象正常地存入和读出 Redis
     */
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("创建RedisTemplate对象");

        // 1. 创建 RedisTemplate 实例
        RedisTemplate redisTemplate = new RedisTemplate<>();

        // 2. 设置 Redis 连接工厂（Spring Boot 自动注入，包含了 Redis 服务器的地址、端口、密码等）
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        /*
         * 3. 设置 Key 的序列化器为 StringRedisSerializer
         *    含义：Redis 的 Key 统一使用 String 类型存储，方便查看和管理
         *    例如：执行 set("dish_1", ...) 时，Key "dish_1" 会以普通字符串形式存入 Redis
         */
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        /*
         * 4. 创建 Jackson 的 ObjectMapper，并配置它的能力
         *    ObjectMapper 是 Jackson 框架的核心类，负责 Java 对象 <-> JSON 之间的转换
         */
        ObjectMapper objectMapper = new ObjectMapper();

        /*
         * 4.1 注册 JavaTimeModule（Java 时间模块）
         *     作用：让 Jackson 能够正确序列化 Java 8 的时间类型
         *     例如：LocalDateTime、LocalDate、LocalTime 等
         *     如果没有这个模块，LocalDateTime 类型的字段（如 updateTime）会序列化失败
         */
        objectMapper.registerModule(new JavaTimeModule());

        /*
         * 4.2 启用默认类型信息（多态类型处理）
         *
         *     参数1 - BasicPolymorphicTypeValidator：
         *           类型校验器，允许所有以 Object 为基类的类型写入类型信息
         *
         *     参数2 - DefaultTyping.NON_FINAL：
         *           类型信息写入策略：只对非 final 的类写入类型信息
         *           例如：ArrayList 不是 final 的，会写入 "@class": "java.util.ArrayList"
         *                String 是 final 的，不会写入类型信息（节省空间）
         *
         *     参数3 - JsonTypeInfo.As.PROPERTY：
         *           类型信息的存储方式：以 JSON 属性的形式存储
         *           序列化后 JSON 中会多一个 "@class" 字段，记录原始 Java 类型
         *
         *     为什么要这样做？
         *     例如：存入 List<DishVO> 后，取出时如果没有类型信息，Jackson 只能还原成
         *          List<LinkedHashMap>，而不是 List<DishVO>
         *          有了 "@class" 字段，Jackson 就能知道应该还原成 DishVO 类型
         */
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        /*
         * 5. 创建 Jackson2JsonRedisSerializer 作为 Value 的序列化器
         *    泛型参数 Object.class 表示：可以序列化任意类型的 Java 对象
         *    然后将上面配置好的 ObjectMapper 设置进去
         *
         *    效果：存入 Redis 时，Java 对象 -> JSON 字符串 -> byte[] 存入 Redis
         *         取出 Redis 时，byte[] -> JSON 字符串 -> Java 对象（根据 @class 还原类型）
         */
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        serializer.setObjectMapper(objectMapper);
        redisTemplate.setValueSerializer(serializer);

        // 6. 返回配置好的 RedisTemplate，交给 Spring 容器管理
        return redisTemplate;
    }

    @Bean
    public CommandLineRunner initShopStatus(StringRedisTemplate stringRedisTemplate) {
        return args -> {
            String key = "SHOP_STATUS";
            Boolean hasKey = stringRedisTemplate.hasKey(key);
            if (Boolean.TRUE.equals(hasKey)) {
                String status = stringRedisTemplate.opsForValue().get(key);
                log.info("========== 启动检测: SHOP_STATUS已存在, 当前值={} ==========", status);
            } else {
                stringRedisTemplate.opsForValue().set(key, "1");
                log.info("========== 启动初始化: SHOP_STATUS不存在, 已默认设为营业中(1) ==========");
            }
        };
    }
}